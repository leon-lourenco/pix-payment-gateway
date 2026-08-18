package com.pixgateway.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateTransactionRequest(
        @Schema(example = "alice@example.com") @NotBlank String payerAccount,
        @Schema(example = "bob@example.com") @NotBlank String payeeAccount,
        @Schema(example = "5000", description = "Amount in integer cents") @Positive long amountCents) {
}
