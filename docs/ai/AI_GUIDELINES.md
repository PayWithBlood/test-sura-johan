# AI assistant guidelines — Payments Integration
## Allowed use and data
Approved AI tools may explain public docs, draft non-sensitive code/tests and improve clarity. Never enter production secrets, access tokens, payer names/IDs/emails, payment tokens, raw webhook bodies, Salesforce exports, or non-public employer data. Use synthetic fixtures and remove identifying metadata.
## Validation and disclosure
The author remains accountable. Review generated diffs line by line; check provider specs and ADRs; run unit, contract, security and static-analysis checks; test failure/replay paths; obtain peer review for payment/policy logic. Do not merge code you cannot explain or claim tests not run. PR disclosure names the tool, assisted areas, checks performed, and material suggestions rejected.
## Service calls to an LLM
Require documented need and privacy/security/legal review. Minimize and redact input; never send raw DLQ bodies or identifiers. Use an approved private endpoint, managed identity, encryption, egress and retention controls. Treat output as untrusted: no direct Salesforce writes, policy decisions or automated replay. Require citations, abstention, human approval, prompt-injection defenses, schema checks, audit without sensitive content, bias/leakage evaluation, rate/cost limits and rollback. Deterministic rules remain authoritative.
