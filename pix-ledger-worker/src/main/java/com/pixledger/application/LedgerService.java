package com.pixledger.application;

import com.pixledger.domain.LedgerEntry;
import com.pixledger.infrastructure.persistence.LedgerEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Posts a transaction to the ledger idempotently. Kafka is at-least-once, so the same event can
 * be delivered more than once; the unique constraint on (transaction_id, direction) is the real
 * guard. Both entries are written in one {@link TransactionTemplate} block so a redelivery either
 * finds nothing and posts both, or collides on the debit insert and rolls back before ever
 * attempting the credit insert — the ledger never ends up with just one leg of a pair.
 */
@Service
public class LedgerService {

    private static final Logger log = LoggerFactory.getLogger(LedgerService.class);

    private final LedgerEntryRepository repository;
    private final TransactionTemplate transactionTemplate;

    public LedgerService(LedgerEntryRepository repository, PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public void postTransaction(TransactionCreatedEvent event) {
        try {
            transactionTemplate.executeWithoutResult(status -> postEntries(event));
        } catch (DataIntegrityViolationException alreadyPosted) {
            log.info("Transaction {} already posted to the ledger, skipping redelivery", event.transactionId());
        }
    }

    private void postEntries(TransactionCreatedEvent event) {
        repository.saveAndFlush(LedgerEntry.debit(event.transactionId(), event.payerAccount(), event.amountCents()));
        repository.saveAndFlush(LedgerEntry.credit(event.transactionId(), event.payeeAccount(), event.amountCents()));
    }
}
