package com.pixledger.infrastructure.kafka;

import com.pixledger.application.LedgerService;
import com.pixledger.application.TransactionCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class TransactionCreatedListener {

    private final LedgerService ledgerService;
    private final ObjectMapper objectMapper;

    public TransactionCreatedListener(LedgerService ledgerService, ObjectMapper objectMapper) {
        this.ledgerService = ledgerService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${pix.kafka.topic.transaction-created:transactions.created}")
    public void onMessage(String payload) {
        TransactionCreatedEvent event = objectMapper.readValue(payload, TransactionCreatedEvent.class);
        ledgerService.postTransaction(event);
    }
}
