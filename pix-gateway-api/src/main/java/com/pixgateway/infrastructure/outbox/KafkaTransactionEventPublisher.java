package com.pixgateway.infrastructure.outbox;

import com.pixgateway.application.port.TransactionEventPublisher;
import com.pixgateway.domain.OutboxEvent;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes outbox events to Redpanda (Kafka API) with the transaction id as the message key, so
 * every event for the same transaction lands on the same partition and stays in order.
 *
 * The event payload is already a JSON string in the outbox table, so it's sent as-is; there's no
 * second serialization step or schema registry involved.
 */
@Component
public class KafkaTransactionEventPublisher implements TransactionEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public KafkaTransactionEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${pix.kafka.topic.transaction-created:transactions.created}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(OutboxEvent event) {
        try {
            kafkaTemplate.send(topic, event.getAggregateId().toString(), event.getPayload())
                    .get(5, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException("Failed to publish outbox event " + event.getId() + " to Kafka", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while publishing outbox event " + event.getId(), e);
        }
    }
}
