package com.tamizh.ledgerflow.service;

import com.tamizh.ledgerflow.core.PaymentStatus;
import com.tamizh.ledgerflow.persistence.PaymentRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentExecutionService {
    private final PaymentRepository payments;

    public PaymentExecutionService(PaymentRepository payments) {
        this.payments = payments;
    }

    @Async("paymentExecutor")
    @Transactional
    public void process(UUID paymentId) {
        var payment = payments.findById(paymentId).orElseThrow();
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }

        payment.transitionTo(PaymentStatus.PROCESSING);
        payment.transitionTo(PaymentStatus.SUCCEEDED);
    }
}
