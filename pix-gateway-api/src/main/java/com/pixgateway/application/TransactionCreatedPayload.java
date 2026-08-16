package com.pixgateway.application;

import com.pixgateway.domain.Transaction;
import java.util.UUID;

record TransactionCreatedPayload(UUID transactionId, String payerAccount, String payeeAccount, long amountCents) {

    static TransactionCreatedPayload from(Transaction transaction) {
        return new TransactionCreatedPayload(
                transaction.getId(), transaction.getPayerAccount(), transaction.getPayeeAccount(), transaction.getAmountCents());
    }
}
