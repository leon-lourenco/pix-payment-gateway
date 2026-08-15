package com.leongomes.pixgateway.infrastructure.outbox;

import com.leongomes.pixgateway.application.port.TransactionEventPublisher;
import com.leongomes.pixgateway.domain.OutboxEvent;
import com.leongomes.pixgateway.infrastructure.persistence.OutboxEventRepository;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Polls the outbox table for events written by {@code TransactionService} and hands them to the
 * configured {@link TransactionEventPublisher}. Runs on a fixed delay rather than reacting to
 * writes directly, since the whole point of the outbox pattern is decoupling the write from the
 * publish.
 */
@Component
public class OutboxDispatcher {

    private final OutboxEventRepository outboxEventRepository;
    private final TransactionEventPublisher eventPublisher;

    public OutboxDispatcher(OutboxEventRepository outboxEventRepository, TransactionEventPublisher eventPublisher) {
        this.outboxEventRepository = outboxEventRepository;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(fixedDelayString = "${pix.outbox.poll-interval-ms:1000}")
    @Transactional
    public void dispatchPendingEvents() {
        List<OutboxEvent> pending = outboxEventRepository.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();
        for (OutboxEvent event : pending) {
            eventPublisher.publish(event);
            event.markPublished();
        }
    }
}
