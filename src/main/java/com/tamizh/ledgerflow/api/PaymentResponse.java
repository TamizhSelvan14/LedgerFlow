package com.tamizh.ledgerflow.api;

import com.tamizh.ledgerflow.persistence.PaymentEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        BigDecimal amount,
        String currency,
        String status,
        Instant updatedAt) {

    public static PaymentResponse from(PaymentEntity payment) {
        return new PaymentResponse(
                payment.getId(), payment.getAmount(), payment.getCurrency(),
                payment.getStatus().name(), payment.getUpdatedAt());
    }
}
