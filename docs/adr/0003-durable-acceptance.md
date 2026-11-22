# ADR-0003: Durable inbox acceptance boundary
- **Status:** Accepted
- **Date:** 2026-11-21
## Context
NFR3 defines acceptance as 2xx. In-memory dispatch can lose acknowledged work; DB and broker have no shared transaction.
## Options
In-memory then 2xx is fast but loses work on restart. Durable conditional inbox + Change Feed is safe but relay can duplicate.
## Decision
Return 2xx only after inbox commit. Relay at least once; dedupe via deterministic event ID and idempotent consumer.
## Consequences
- Positive: acceptance is auditable and durable.
- Negative: store failure means 503 and provider retry.
- Mitigation: monitor feed lag; reconcile unrelayed records.
