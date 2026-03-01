package com.tamizh.ledgerflow.core;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class InMemoryPaymentProcessor implements AutoCloseable {
    private final Map<String, CompletableFuture<PaymentSnapshot>> requests = new ConcurrentHashMap<>();
    private final Map<UUID, PaymentSnapshot> payments = new ConcurrentHashMap<>();
    private final ExecutorService workers;

    public InMemoryPaymentProcessor(int workerCount) {
        if (workerCount <= 0) {
            throw new IllegalArgumentException("workerCount must be positive");
        }
        workers = Executors.newFixedThreadPool(workerCount);
    }

    public PaymentSnapshot submit(String idempotencyKey, PaymentCommand command) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("idempotency key is required");
        }

        var created = new CompletableFuture<PaymentSnapshot>();
        var existing = requests.putIfAbsent(idempotencyKey, created);
        if (existing != null) {
            return existing.join();
        }

        try {
            var payment = new PaymentSnapshot(
                    UUID.randomUUID(), idempotencyKey, command.amount(), command.currency(),
                    PaymentStatus.PENDING, Instant.now());
            payments.put(payment.id(), payment);
            created.complete(payment);
            return payment;
        } catch (RuntimeException error) {
            created.completeExceptionally(error);
            requests.remove(idempotencyKey, created);
            throw error;
        }
    }

    public CompletableFuture<PaymentSnapshot> processAsync(UUID paymentId) {
        return CompletableFuture.supplyAsync(() -> {
            transition(paymentId, PaymentStatus.PENDING, PaymentStatus.PROCESSING);
            return transition(paymentId, PaymentStatus.PROCESSING, PaymentStatus.SUCCEEDED);
        }, workers);
    }

    public PaymentSnapshot get(UUID paymentId) {
        var payment = payments.get(paymentId);
        if (payment == null) {
            throw new IllegalArgumentException("payment not found: " + paymentId);
        }
        return payment;
    }

    public int paymentCount() {
        return payments.size();
    }

    private PaymentSnapshot transition(UUID paymentId, PaymentStatus expected, PaymentStatus next) {
        return payments.compute(paymentId, (id, current) -> {
            if (current == null) {
                throw new IllegalArgumentException("payment not found: " + id);
            }
            if (current.status() != expected) {
                throw new IllegalStateException("expected " + expected + " but was " + current.status());
            }
            return new PaymentSnapshot(
                    current.id(), current.idempotencyKey(), current.amount(), current.currency(),
                    next, Instant.now());
        });
    }

    @Override
    public void close() {
        workers.shutdownNow();
    }
}
