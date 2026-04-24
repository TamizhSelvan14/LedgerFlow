package com.tamizh.ledgerflow.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentWorker {
    private final PaymentExecutionService executionService;

    public PaymentWorker(PaymentExecutionService executionService) {
        this.executionService = executionService;
    }

    @KafkaListener(topics = "payment.requested", groupId = "ledgerflow-workers")
    public void receive(String payload) {
        var marker = "\"paymentId\":\"";
        var start = payload.indexOf(marker) + marker.length();
        var end = payload.indexOf('"', start);
        executionService.process(UUID.fromString(payload.substring(start, end)));
    }
}
