package com.leongomes.pixgateway.application.port;

import com.leongomes.pixgateway.domain.OutboxEvent;

/**
 * Outbound port for delivering domain events to whatever broker sits behind it.
 *
 * The application layer only depends on this interface. In this phase the only adapter is a
 * logging stand-in; a Kafka/Redpanda adapter will be plugged in later without any change here.
 */
public interface TransactionEventPublisher {

    void publish(OutboxEvent event);
}
