package com.pixledger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.pixledger.application.TransactionCreatedEvent;
import com.pixledger.domain.LedgerDirection;
import com.pixledger.domain.LedgerEntry;
import com.pixledger.infrastructure.persistence.LedgerEntryRepository;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.ObjectMapper;

/**
 * Publishes directly to the topic the way pix-gateway-api's outbox dispatcher would, rather than
 * calling {@code LedgerService} in-process, so this exercises the real Kafka deserialization and
 * listener wiring, not just the posting logic.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class LedgerPostingIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${pix.kafka.topic.transaction-created}")
    private String topic;

    @AfterEach
    void cleanUpDatabase() {
        ledgerEntryRepository.deleteAll();
    }

    @Test
    void consumingTransactionCreatedEventPostsBalancedLedgerEntries() {
        UUID transactionId = UUID.randomUUID();
        publishTransactionCreated(transactionId);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<LedgerEntry> entries = ledgerEntryRepository.findByTransactionId(transactionId);
            assertThat(entries).hasSize(2);
            assertThat(entries).extracting(LedgerEntry::getDirection)
                    .containsExactlyInAnyOrder(LedgerDirection.DEBIT, LedgerDirection.CREDIT);
        });
    }

    @Test
    void redeliveredEventDoesNotDoublePostTheLedger() {
        UUID transactionId = UUID.randomUUID();
        publishTransactionCreated(transactionId);
        await().atMost(Duration.ofSeconds(10))
                .until(() -> ledgerEntryRepository.findByTransactionId(transactionId).size() == 2);

        // Simulates Kafka's at-least-once delivery redelivering the same message.
        publishTransactionCreated(transactionId);

        await().pollDelay(Duration.ofSeconds(2)).atMost(Duration.ofSeconds(10)).untilAsserted(
                () -> assertThat(ledgerEntryRepository.findByTransactionId(transactionId)).hasSize(2));
    }

    private void publishTransactionCreated(UUID transactionId) {
        String payload = objectMapper.writeValueAsString(
                new TransactionCreatedEvent(transactionId, "alice@example.com", "bob@example.com", 5_000L));
        kafkaTemplate.send(topic, transactionId.toString(), payload);
    }
}
