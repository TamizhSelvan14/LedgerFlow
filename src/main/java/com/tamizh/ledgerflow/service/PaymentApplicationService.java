package com.tamizh.ledgerflow.service;

import com.tamizh.ledgerflow.persistence.OutboxEventEntity;
import com.tamizh.ledgerflow.persistence.OutboxEventRepository;
import com.tamizh.ledgerflow.persistence.PaymentEntity;
import com.tamizh.ledgerflow.persistence.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentApplicationService {
    private final PaymentRepository payments;
    private final OutboxEventRepository outbox;

    public PaymentApplicationService(PaymentRepository payments, OutboxEventRepository outbox) {
        this.payments = payments;
        this.outbox = outbox;
    }

    @Transactional
    public PaymentEntity create(String idempotencyKey, BigDecimal amount, String currency) {
        return payments.findByIdempotencyKey(idempotencyKey).orElseGet(() -> {
            var payment = payments.save(new PaymentEntity(
                    UUID.randomUUID(), idempotencyKey, amount, currency));
            var payload = "{\"paymentId\":\"" + payment.getId() + "\"}";
            outbox.save(new OutboxEventEntity(payment.getId(), "payment.requested", payload));
            return payment;
        });
    }

    @Transactional(readOnly = true)
    public PaymentEntity get(UUID id) {
        return payments.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("payment not found: " + id));
    }
}
