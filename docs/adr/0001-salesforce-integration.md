# ADR-0001: Asynchronous batched Salesforce upserts
- **Status:** Accepted
- **Date:** 2026-11-21
## Context
Salesforce is ≈97% available, shared API budget is 150,000 calls/day for this integration, max 25 concurrent requests; providers timeout in 5–10 seconds. Incorrect stale status can affect policy coverage.
## Options
### Synchronous single-record webhook write
- Pros: simple, immediate.
- Cons: couples timeout to Salesforce; loses availability and uses one API call per message.
### Durable async queue and composite upsert
- Pros: backpressure, replay, outage tolerance, up to 200 records/call.
- Cons: eventual consistency; inbox, DLQ and reconciliation required.
## Decision
Commit durable inbox before 2xx, relay to Service Bus, serialize per payment and batch 100 records/2s with `allOrNone:false`. Upsert by `Payment_Key__c`; cache OAuth token; guard stale timestamps. Never update policy status.
## Consequences
- Positive: keeps provider intake available and peak-day calls ≈1,200.
- Negative: eventual updates and operator DLQ duties.
- Risks: isolate per-record failures; retry 5xx/locks with jitter; circuit-break API-limit 403; correct invalid references before replay.
