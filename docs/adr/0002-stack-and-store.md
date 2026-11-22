# ADR-0002: Java 21, Spring Boot 3 and Cosmos DB
- **Status:** Accepted
- **Date:** 2026-11-21
## Context
Raw-byte HMAC, deterministic mapping, idempotency and automated tests are required. Java 21 is accepted and matches the supplied Java/Spring prototype and IntelliJ workspace.
## Options
### Mule 4 / DataWeave / MUnit
- Pros: natural enterprise integration/API Manager fit.
- Cons: licensed runtime dependencies may not be available for this take-home.
### Java 21 / Spring Boot 3
- Pros: aligns with prototype; standard crypto, tests and adapter interfaces.
- Cons: production Azure connectors/runtime must be implemented; APIM remains separate.
### Store
SQL offers relational transactions but needs outbox polling. Cosmos supports payment-partition ETags and Change Feed, at the cost of partition/consistency design.
## Decision
Use Java 21/Spring Boot 3 and Cosmos DB. Part B uses interfaces/stubs; no real Azure, broker, Key Vault or Salesforce calls.
## Consequences
- Positive: independently testable; payment partition supports serialization.
- Negative: local component is not a production Azure connector.
- Mitigate with exact vectors, pinned dependencies, managed identity and load testing before production.
