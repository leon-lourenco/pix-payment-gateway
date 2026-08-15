package com.leongomes.pixgateway.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * A single money movement between two PIX accounts.
 *
 * Money is stored as integer cents ({@code amountCents}) to keep the ledger free of
 * floating-point rounding errors.
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "payer_account", nullable = false)
    private String payerAccount;

    @Column(name = "payee_account", nullable = false)
    private String payeeAccount;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Transaction() {
        // required by JPA
    }

    public static Transaction createPending(String idempotencyKey, String payerAccount, String payeeAccount, long amountCents) {
        Transaction transaction = new Transaction();
        transaction.id = UUID.randomUUID();
        transaction.idempotencyKey = idempotencyKey;
        transaction.payerAccount = payerAccount;
        transaction.payeeAccount = payeeAccount;
        transaction.amountCents = amountCents;
        transaction.status = TransactionStatus.PENDING;
        Instant now = Instant.now();
        transaction.createdAt = now;
        transaction.updatedAt = now;
        return transaction;
    }

    public UUID getId() {
        return id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getPayerAccount() {
        return payerAccount;
    }

    public String getPayeeAccount() {
        return payeeAccount;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
