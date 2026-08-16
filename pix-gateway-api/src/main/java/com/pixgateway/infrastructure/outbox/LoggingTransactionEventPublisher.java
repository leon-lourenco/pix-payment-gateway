package com.pixgateway.infrastructure.outbox;

import com.pixgateway.application.port.TransactionEventPublisher;
import com.pixgateway.domain.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Placeholder adapter for this phase: logs the event instead of publishing it to a broker.
 * Phase 2 replaces this with a Redpanda/Kafka-backed {@link TransactionEventPublisher}; nothing
 * outside this class needs to change when that happens.
 */
@Component
public class LoggingTransactionEventPublisher implements TransactionEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingTransactionEventPublisher.class);

    @Override
    public void publish(OutboxEvent event) {
        log.info("outbox event published: id={} type={} aggregateId={} payload={}",
                event.getId(), event.getEventType(), event.getAggregateId(), event.getPayload());
    }
}
