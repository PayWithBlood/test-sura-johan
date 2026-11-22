# Assumptions
| # | Assumption | Why | Parts |
|---|---|---|---|
| 1 | APIM supports private routing, body/rate limits. | Tier unspecified. | A–C |
| 2 | Cosmos Change Feed relay is at least once; duplicates safe. | No cross-service transaction. | A–C |
| 3 | SF key `provider:transactionId` is unique and fits Text(80). | Business upsert. | A |
| 4 | Andina ref must validate as uppercase `POL-CO-NNNNNNNNN`; malformed needs remediation. | v2.3 payer-entered references. | A–D |
| 5 | Andina event ID = transaction + state + occurredAt; equal-time conflicts reviewed. | Attempt ID changes. | A–B |
| 6 | Later provider occurredAt wins; conflicting equal-time states quarantined. | No provider ordering. | A |
| 7 | Salesforce composite upsert/currencies behave as supplied. | Design dependency. | A |
| 8 | Partial Quetzal refund maps to Refunded; Finance confirms balance treatment. | Salesforce lacks refunded amount field. | A |
| 9 | Workforce OIDC provides separate read/replay roles. | Operator API unspecified. | A,D |
| 10 | Raw encrypted ≤30 days, status ledger 13 months, pending privacy/legal approval. | Retention law absent. | A,C |
| 11 | Test vectors use injected clock because fixtures can be future-dated. | Deterministic tests. | B |
| 12 | Candidate ID, actual hours, final SHA/time supplied by candidate. | Avoid invented facts. | Submission |
