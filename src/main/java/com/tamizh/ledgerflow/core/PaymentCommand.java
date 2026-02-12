package com.tamizh.ledgerflow.core;

import java.math.BigDecimal;

public record PaymentCommand(BigDecimal amount, String currency) {
    public PaymentCommand {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        if (currency == null || !currency.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("currency must be a three-letter uppercase code");
        }
    }
}
