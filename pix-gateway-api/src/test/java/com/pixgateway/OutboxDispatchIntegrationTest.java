package com.pixgateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.pixgateway.domain.OutboxEvent;
import com.pixgateway.infrastructure.persistence.OutboxEventRepository;
import com.pixgateway.infrastructure.persistence.TransactionRepository;
import com.pixgateway.infrastructure.web.dto.CreateTransactionRequest;
import com.pixgateway.infrastructure.web.dto.TransactionResponse;
import java.time.Duration;
import java.util.UUID;
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
 * Confirms the outbox dispatcher actually gets an event to Redpanda, not just that it gets
 * written to the outbox table. {@code publishedAt} only flips once
 * {@link com.pixgateway.infrastructure.outbox.KafkaTransactionEventPublisher} has a broker
 * acknowledgment in hand, so this is an end-to-end check of the producer wiring, not a stub.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class OutboxDispatchIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @AfterEach
    void cleanUpDatabase() {
        outboxEventRepository.deleteAll();
        transactionRepository.deleteAll();
    }

    @Test
    void outboxEventIsPublishedToKafkaShortlyAfterTransactionIsCreated() {
        String idempotencyKey = UUID.randomUUID().toString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Idempotency-Key", idempotencyKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateTransactionRequest> request = new HttpEntity<>(
                new CreateTransactionRequest("alice@example.com", "bob@example.com", 5_000L), headers);
        ResponseEntity<TransactionResponse> response =
                restTemplate.postForEntity("/transactions", request, TransactionResponse.class);
        UUID transactionId = response.getBody().id();

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            OutboxEvent event = outboxEventRepository.findByAggregateId(transactionId).orElseThrow();
            assertThat(event.getPublishedAt()).isNotNull();
        });
    }
}
