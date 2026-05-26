# LedgerFlow

LedgerFlow is an idempotent, event-driven payment-processing service built with Java 17, Spring Boot, PostgreSQL, Kafka, Flyway, JUnit, Testcontainers, and Docker.

## Design

- `POST /api/v1/payments` creates a payment and requires an `Idempotency-Key` header.
- `GET /api/v1/payments/{id}` returns the latest payment state.
- A PostgreSQL unique constraint prevents duplicate payments for the same key.
- Payment creation and outbox insertion occur in one database transaction.
- A scheduled outbox publisher sends `payment.requested` events to Kafka.
- Kafka consumers dispatch work through a bounded Spring executor.
- Optimistic locking protects concurrent state transitions.
- States are `PENDING`, `PROCESSING`, `SUCCEEDED`, and `FAILED`.

## Run locally

```bash
docker compose up --build
```

Create a payment:

```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: order-123' \
  -d '{"amount":42.50,"currency":"USD"}'
```

## Tests

Run the complete Spring/Testcontainers suite in an environment with Maven and Docker:

```bash
mvn test
```

The dependency-free concurrency and idempotency core can be tested with only Java:

```bash
./scripts/test-core.sh
```
