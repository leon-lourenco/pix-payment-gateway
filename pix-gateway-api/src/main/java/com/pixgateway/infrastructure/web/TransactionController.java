package com.pixgateway.infrastructure.web;

import com.pixgateway.application.CreateTransactionCommand;
import com.pixgateway.application.TransactionService;
import com.pixgateway.domain.Transaction;
import com.pixgateway.infrastructure.web.dto.CreateTransactionRequest;
import com.pixgateway.infrastructure.web.dto.TransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
@Validated
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @Operation(
            summary = "Create a PIX transaction",
            description = "Accepts a transaction for asynchronous settlement. Replaying the same "
                    + "Idempotency-Key returns the original transaction instead of creating a "
                    + "duplicate, whether the replay is sequential or concurrent with the first request.")
    @ApiResponse(responseCode = "202", description = "Transaction accepted (or replayed idempotently)",
            content = @Content(schema = @Schema(implementation = TransactionResponse.class)))
    @ApiResponse(responseCode = "400", description = "Missing header or invalid request body",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<TransactionResponse> create(
            @Parameter(description = "Client-generated key; replays with the same key are safe to retry",
                    required = true, example = "b3b3f6b0-6e0a-4c9a-9c0a-1f2e3d4c5b6a")
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @Valid @RequestBody CreateTransactionRequest request) {

        Transaction transaction = transactionService.createTransaction(new CreateTransactionCommand(
                idempotencyKey, request.payerAccount(), request.payeeAccount(), request.amountCents()));

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(TransactionResponse.from(transaction));
    }
}
