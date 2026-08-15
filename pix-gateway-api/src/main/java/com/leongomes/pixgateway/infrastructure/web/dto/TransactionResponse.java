package com.leongomes.pixgateway.infrastructure.web.dto;

import com.leongomes.pixgateway.domain.Transaction;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        String status,
        String payerAccount,
        String payeeAccount,
        long amountCents,
        Instant createdAt) {

    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getStatus().name(),
                transaction.getPayerAccount(),
                transaction.getPayeeAccount(),
                transaction.getAmountCents(),
                transaction.getCreatedAt());
    }
}
