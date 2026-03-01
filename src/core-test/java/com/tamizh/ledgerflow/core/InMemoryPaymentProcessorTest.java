package com.tamizh.ledgerflow.core;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public final class InMemoryPaymentProcessorTest {
    public static void main(String[] args) throws Exception {
        duplicateRequestsReturnTheSamePayment();
        concurrentRequestsCreateOnePayment();
        paymentTransitionsToSucceeded();
        invalidAmountsAreRejected();
        System.out.println("LedgerFlow core tests passed: 4/4");
    }

    private static void duplicateRequestsReturnTheSamePayment() {
        try (var processor = new InMemoryPaymentProcessor(2)) {
            var command = new PaymentCommand(new BigDecimal("42.50"), "USD");
            var first = processor.submit("order-123", command);
            var second = processor.submit("order-123", command);
            assert first.id().equals(second.id());
            assert processor.paymentCount() == 1;
        }
    }

    private static void concurrentRequestsCreateOnePayment() throws Exception {
        try (var processor = new InMemoryPaymentProcessor(4)) {
            var callers = Executors.newFixedThreadPool(12);
            try {
                var command = new PaymentCommand(new BigDecimal("99.99"), "USD");
                var tasks = new ArrayList<Callable<PaymentSnapshot>>();
                for (int i = 0; i < 100; i++) {
                    tasks.add(() -> processor.submit("same-order", command));
                }
                var ids = new HashSet<>();
                for (var result : callers.invokeAll(tasks)) {
                    ids.add(result.get().id());
                }
                assert ids.size() == 1;
                assert processor.paymentCount() == 1;
            } finally {
                callers.shutdownNow();
            }
        }
    }

    private static void paymentTransitionsToSucceeded() {
        try (var processor = new InMemoryPaymentProcessor(2)) {
            var created = processor.submit(
                    "order-456", new PaymentCommand(new BigDecimal("10.00"), "EUR"));
            var completed = processor.processAsync(created.id()).join();
            assert completed.status() == PaymentStatus.SUCCEEDED;
            assert processor.get(created.id()).status() == PaymentStatus.SUCCEEDED;
        }
    }

    private static void invalidAmountsAreRejected() {
        var rejected = false;
        try {
            new PaymentCommand(BigDecimal.ZERO, "USD");
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        assert rejected;
    }
}
