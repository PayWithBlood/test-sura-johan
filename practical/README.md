# Practical intake — Mexico and Colombia

## Stack and run
Java 21, Spring Boot 3.3.13, Maven and JUnit 5. From this directory run `mvn test`. For a local HTTP demo run `mvn spring-boot:run`; by default it binds only to `127.0.0.1:8080` and uses the `local` profile with fictional test secrets. Routes are `POST /webhooks/quetzal` and `POST /webhooks/andina`.

## What is implemented
The controller accepts raw request bytes, signature/timestamp headers and optional correlation ID. The intake verifies provider-specific HMAC/timestamp, validates event type/currency and policy reference, maps a canonical event, and sends it through `Inbox` and `EventPublisher` ports. MockMvc tests exercise both actual HTTP routes; service tests cover provider vectors, retries, invalid signatures and malformed policy references.

## Stubs and known gaps
The local profile uses an in-memory inbox and event queue; they are intentionally non-durable and must never be used in production. Production requires Cosmos conditional inbox/outbox, Change Feed, Service Bus sessions, Key Vault secrets, APIM policies, Salesforce worker/batching, reconciliation and DLQ/replay adapters. The local demo therefore does not claim NFR3 durability or production readiness. Quetzal partial-refund accounting and retention require business/privacy approval. No external service is called by the tests.
