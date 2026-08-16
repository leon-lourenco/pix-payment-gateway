package com.pixgateway.application;

import com.pixgateway.domain.OutboxEvent;
import com.pixgateway.domain.Transaction;
import com.pixgateway.infrastructure.persistence.OutboxEventRepository;
import com.pixgateway.infrastructure.persistence.TransactionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

/**
 * Creates transactions idempotently: replaying the same idempotency key returns the original
 * transaction instead of creating a duplicate.
 *
 * The happy path and the duplicate path are deliberately two separate database transactions
 * rather than a single "check, then insert" in one transaction. Two callers can pass the
 * existence check at the same time, so the real guard is the unique constraint on
 * {@code idempotency_key}; whichever insert loses the race fails with
 * {@link DataIntegrityViolationException}. On Postgres, a failed statement poisons the rest of
 * that transaction, so the fallback lookup has to run in a new one — {@link TransactionTemplate}
 * guarantees the first transaction has fully rolled back before {@code execute()} returns.
 */
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final TransactionTemplate transactionTemplate;
    private final ObjectMapper objectMapper;

    public TransactionService(TransactionRepository transactionRepository,
                               OutboxEventRepository outboxEventRepository,
                               PlatformTransactionManager transactionManager,
                               ObjectMapper objectMapper) {
        this.transactionRepository = transactionRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.objectMapper = objectMapper;
    }

    public Transaction createTransaction(CreateTransactionCommand command) {
        try {
            return transactionTemplate.execute(status -> persistTransactionAndOutboxEvent(command));
        } catch (DataIntegrityViolationException duplicateIdempotencyKey) {
            return transactionRepository.findByIdempotencyKey(command.idempotencyKey())
                    .orElseThrow(() -> duplicateIdempotencyKey);
        }
    }

    private Transaction persistTransactionAndOutboxEvent(CreateTransactionCommand command) {
        Transaction transaction = Transaction.createPending(
                command.idempotencyKey(), command.payerAccount(), command.payeeAccount(), command.amountCents());

        // saveAndFlush forces the INSERT (and a possible unique-constraint violation) to happen
        // here, before the outbox event is written, instead of being deferred to commit time.
        transactionRepository.saveAndFlush(transaction);
        String payload = objectMapper.writeValueAsString(TransactionCreatedPayload.from(transaction));
        outboxEventRepository.save(OutboxEvent.forTransactionCreated(transaction, payload));

        return transaction;
    }
}
