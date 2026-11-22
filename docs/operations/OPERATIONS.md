# Operations — Premium Payment Notifications

## SLIs and SLOs
Per country, measure from provider state-change time to Salesforce committed state: ≥95% within 5 minutes and ≥99.5% within 1 hour. Measure durable acceptance (2xx after inbox commit) and reconciliation: 100% accepted messages traceable to recorded, action-required or DLQ state; no silent loss. Dashboard by country/provider: request counts, signature/schema rejects, accepted duplicates, oldest queue age, outbox lag, Salesforce latency/status/API budget, retries and DLQ depth.

## Correlation and privacy
APIM creates/validates opaque UUID correlation ID and propagates it in Cosmos, Service Bus properties, Salesforce integration metadata where supported, event topic and logs. Log IDs, country/provider, status, timings, retry count and allowlisted error codes only. Never log raw bodies, signatures, secrets, payer names/IDs/emails, payment tokens or access tokens. Redact exception messages from external systems. App Insights sampling excludes failures/DLQ counters; encrypt, restrict access and set retention.

## Alerts and response
- Page on-call: oldest accepted message >4 min (warning at 2m); DLQ depth >0 for any country; accepted-to-recorded mismatch; change-feed lag >2m.
- Page integration + Salesforce owners: REQUEST_LIMIT_EXCEEDED, 401 token refresh failure, Salesforce 5xx >5% for 5m, or worker circuit open.
- Page security: signature failures >5% over 5m or sudden provider-specific spike.
- Daily ticket: API budget >70% rolling 24h; alert at 80%, pause noncritical retries before 90%.
On-call acknowledges SEV-2 within 15 min, opens incident channel per company procedure, records correlation IDs and country impact. Never replay bulk DLQ without validating cause and current payment projection.
