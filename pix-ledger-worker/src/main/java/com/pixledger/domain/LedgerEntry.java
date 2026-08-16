package com.pixledger.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * One leg of a double-entry posting. A single transaction from pix-gateway-api always produces
 * exactly two entries — a DEBIT on the payer's account and a CREDIT on the payee's, both
 * referencing the same {@code transactionId} — so the pair either both exist or neither does.
 */
@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    private UUID id;

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(nullable = false)
    private String account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerDirection direction;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected LedgerEntry() {
        // required by JPA
    }

    public static LedgerEntry debit(UUID transactionId, String account, long amountCents) {
        return create(transactionId, account, LedgerDirection.DEBIT, amountCents);
    }

    public static LedgerEntry credit(UUID transactionId, String account, long amountCents) {
        return create(transactionId, account, LedgerDirection.CREDIT, amountCents);
    }

    private static LedgerEntry create(UUID transactionId, String account, LedgerDirection direction, long amountCents) {
        LedgerEntry entry = new LedgerEntry();
        entry.id = UUID.randomUUID();
        entry.transactionId = transactionId;
        entry.account = account;
        entry.direction = direction;
        entry.amountCents = amountCents;
        entry.createdAt = Instant.now();
        return entry;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public String getAccount() {
        return account;
    }

    public LedgerDirection getDirection() {
        return direction;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
