# Review — PR-214

## Verdict
- [x] Request changes
- [ ] Approve
- [ ] Approve with comments

## Summary for @dvaldes
Thanks for adding the Chile path and sample coverage. Please address the authentication bypass, loss of acknowledged work, PII logging and incorrect status ordering before merge. The present green tests do not exercise those failure modes.

## Findings
| # | Severity | File:line | Finding | Why it matters here | Suggested fix |
|---|---|---|---|---|---|
| 1 | Blocker | `AustralWebhookController.java:45` | Missing signature is accepted because validation only runs when signature is non-null. | Any internet caller can create/alter premium events and cause Salesforce automation to reinstate/suspend coverage. | Require signature header and reject missing/malformed values before parsing or dispatch. |
| 2 | Blocker | `AustralWebhookController.java:51-54` | Logs the entire notification record including payer name, RUT and email. | Violates NFR6 and exposes regulated personal data in logs. | Log event ID/correlation and safe outcome only; explicitly exclude payer fields. |
| 3 | Blocker | `AustralWebhookController.java:56-60`; `AsyncConfig.java:12-14` | Returns 200 after enqueueing in a process-local fixed executor, before durable persistence. Executor queue/process crash can lose acknowledged notifications. | NFR3 says 2xx means no accepted event may be lost; Austral retries are limited. | Commit durable inbox/outbox (or durable broker) before 2xx; return retryable 5xx on failure. |
| 4 | Major | `AustralPaymentService.java:47-49` | Exceptions are swallowed after logging, so work appears completed to the caller/executor with no retry/DLQ state. | Salesforce 97% availability creates failures; notifications can be silently abandoned. | Propagate failure to durable retry/DLQ and record operational status. |
| 5 | Major | `AustralPaymentService.java:34-44` | find-then-create uses multiple calls and races; processed-event insert occurs after external Salesforce write and is not atomic with it. | Concurrent retries can duplicate payments; crashes cause replay ambiguity and waste shared API budget. | Upsert by deterministic Salesforce Payment_Key__c, with durable inbox idempotency and reconciliation. |
| 6 | Major | `AustralPaymentService.java:31-32,39` | Older notification blindly overwrites current status; no occurredAt comparison. | Providers do not guarantee order; late captured can replace a refund and reverse policy automation. | Maintain transition timestamp/version and reject stale updates under per-payment serialization. |
| 7 | Major | `AustralNotification.java:10`; `AustralPaymentService.java:39` | Amount is parsed as double and rounded through binary floating point. | Money can be represented incorrectly; Salesforce currency must be exact. | Use BigDecimal/string and CLP whole-unit validation. |
| 8 | Major | `AustralPaymentService.java:56-60` | Unknown event types default to Pending. | Malformed/new provider events become valid business records instead of being rejected/quarantined. | Explicitly reject unsupported eventType and validate required fields. |
| 9 | Major | `AustralSignatureVerifier.java:25` | Uses String.equals for MAC comparison. | Timing leaks are avoidable for externally supplied authentication data. | Use MessageDigest.isEqual on decoded MAC bytes. |
| 10 | Minor | `AustralWebhookController.java:45-48` | Parses a Java String rather than preserving raw request bytes for signature validation. | Charset conversion can diverge from the provider's exact raw-body signing contract. | Accept byte[] and verify exact bytes before deserialization. |

## What looks good
The handler separates signature verification and service logic, and the author used the supplied sample event. Those are good starting points once durable acceptance and validation are covered.
