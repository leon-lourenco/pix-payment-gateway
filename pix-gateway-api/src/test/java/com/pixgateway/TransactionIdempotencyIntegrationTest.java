package com.pixgateway;

import static org.assertj.core.api.Assertions.assertThat;

import com.pixgateway.infrastructure.persistence.OutboxEventRepository;
import com.pixgateway.infrastructure.persistence.TransactionRepository;
import com.pixgateway.infrastructure.web.dto.CreateTransactionRequest;
import com.pixgateway.infrastructure.web.dto.TransactionResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Exercises the actual failure mode idempotency is meant to prevent: the same client request
 * retried while the first attempt is still in flight, not just a second call made after the
 * first has already committed.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class TransactionIdempotencyIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    // Spring caches and reuses the same application context (and Testcontainers Postgres)
    // across every @Test method here, so row counts must be reset between tests.
    @AfterEach
    void cleanUpDatabase() {
        outboxEventRepository.deleteAll();
        transactionRepository.deleteAll();
    }

    @Test
    void concurrentRequestsWithSameIdempotencyKeyCreateExactlyOneTransaction() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString();
        int concurrentRequests = 10;

        ExecutorService executor = Executors.newFixedThreadPool(concurrentRequests);
        CountDownLatch allThreadsReady = new CountDownLatch(concurrentRequests);
        CountDownLatch fireAllAtOnce = new CountDownLatch(1);

        List<Future<ResponseEntity<TransactionResponse>>> futures = new ArrayList<>();
        for (int i = 0; i < concurrentRequests; i++) {
            futures.add(executor.submit(() -> {
                allThreadsReady.countDown();
                fireAllAtOnce.await();
                return postTransaction(idempotencyKey);
            }));
        }

        allThreadsReady.await();
        fireAllAtOnce.countDown();

        List<UUID> returnedTransactionIds = new ArrayList<>();
        for (Future<ResponseEntity<TransactionResponse>> future : futures) {
            ResponseEntity<TransactionResponse> response = future.get(10, TimeUnit.SECONDS);
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            returnedTransactionIds.add(response.getBody().id());
        }
        executor.shutdown();

        assertThat(new HashSet<>(returnedTransactionIds)).hasSize(1);
        assertThat(transactionRepository.count()).isEqualTo(1);
        assertThat(outboxEventRepository.count()).isEqualTo(1);
    }

    @Test
    void secondSequentialRequestWithSameIdempotencyKeyReturnsOriginalTransaction() {
        String idempotencyKey = UUID.randomUUID().toString();

        ResponseEntity<TransactionResponse> first = postTransaction(idempotencyKey);
        ResponseEntity<TransactionResponse> second = postTransaction(idempotencyKey);

        assertThat(second.getBody().id()).isEqualTo(first.getBody().id());
        assertThat(transactionRepository.count()).isEqualTo(1);
    }

    private ResponseEntity<TransactionResponse> postTransaction(String idempotencyKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Idempotency-Key", idempotencyKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateTransactionRequest> request = new HttpEntity<>(
                new CreateTransactionRequest("acc-payer", "acc-payee", 5_000L), headers);
        return restTemplate.postForEntity("/transactions", request, TransactionResponse.class);
    }
}
