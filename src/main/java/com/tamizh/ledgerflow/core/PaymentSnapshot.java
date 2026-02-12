package com.tamizh.ledgerflow.core;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentSnapshot(
        UUID id,
        String idempotencyKey,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        Instant updatedAt) {
}
