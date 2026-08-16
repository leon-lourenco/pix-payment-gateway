package com.pixgateway.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateTransactionRequest(
        @NotBlank String payerAccount,
        @NotBlank String payeeAccount,
        @Positive long amountCents) {
}
