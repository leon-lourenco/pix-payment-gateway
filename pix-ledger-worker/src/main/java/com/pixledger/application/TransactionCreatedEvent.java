package com.pixledger.application;

import java.util.UUID;

/**
 * Mirrors the JSON shape pix-gateway-api publishes. Deliberately not a shared library between
 * the two services — they're coupled only through this wire contract, not through Java code.
 */
public record TransactionCreatedEvent(UUID transactionId, String payerAccount, String payeeAccount, long amountCents) {
}
