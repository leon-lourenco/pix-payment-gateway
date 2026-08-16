package com.pixgateway.application;

public record CreateTransactionCommand(String idempotencyKey, String payerAccount, String payeeAccount, long amountCents) {
}
