package com.pixgateway.application.port;

import com.pixgateway.domain.OutboxEvent;

/**
 * Outbound port for delivering domain events to whatever broker sits behind it.
 *
 * {@code publish} must not return until the broker has confirmed delivery. The caller
 * ({@code OutboxDispatcher}) marks the event published immediately afterward, so a publish that
 * returns successfully without a real delivery guarantee would let a lost event be forgotten.
 */
public interface TransactionEventPublisher {

    void publish(OutboxEvent event);
}
