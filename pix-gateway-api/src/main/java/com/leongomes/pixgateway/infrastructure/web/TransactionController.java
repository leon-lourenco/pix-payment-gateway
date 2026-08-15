package com.leongomes.pixgateway.infrastructure.web;

import com.leongomes.pixgateway.application.CreateTransactionCommand;
import com.leongomes.pixgateway.application.TransactionService;
import com.leongomes.pixgateway.domain.Transaction;
import com.leongomes.pixgateway.infrastructure.web.dto.CreateTransactionRequest;
import com.leongomes.pixgateway.infrastructure.web.dto.TransactionResponse;
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
    public ResponseEntity<TransactionResponse> create(
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @Valid @RequestBody CreateTransactionRequest request) {

        Transaction transaction = transactionService.createTransaction(new CreateTransactionCommand(
                idempotencyKey, request.payerAccount(), request.payeeAccount(), request.amountCents()));

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(TransactionResponse.from(transaction));
    }
}
