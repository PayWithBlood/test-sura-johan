# Azure services and cost rationale
| Service | Choice | Alternative declined |
|---|---|---|
| API Manager | Existing gateway for TLS, routes, quotas and backend identity. | Public app endpoint bypasses required governance. |
| Container Apps | Java intake/worker; autoscale on queue age/concurrency. | AKS adds cluster overhead; Functions may suit relay but is less natural for long-lived HTTP. |
| Cosmos DB | Serverless dev/test, provisioned/autoscale prod; payment partition, ETag and Change Feed. | SQL needs outbox poller; Cosmos scales with burst. |
| Service Bus Premium | Sessions, retries/DLQ, topic fan-out and private networking. | Storage Queue lacks sessions/topics; Kafka is heavier. |
| Key Vault | Provider/Salesforce secrets with RBAC and rotation. | Config files expose credentials. |
| Log Analytics / App Insights | Redacted traces, queue-age and SLO signals. | Raw request-body logging prohibited. |

## Cost estimate
Indicative order-of-magnitude USD/month, one region and modest production baseline: APIM already platform-funded ($0 incremental); 2 Service Bus Premium units $1,000–1,500; Container Apps $150–500 at average 1–3 vCPU; Cosmos $100–600 by RU/storage; Key Vault, monitoring and private endpoints $100–500 (100–300 GB logs). Total ≈$1.4k–3.1k excluding APIM, Salesforce, support and tax. Region, retention, peak scaling and premium units can change this materially; validate in Azure calculator after load test. Control cost with autoscale, log sampling, short raw-body TTL, budgets and stopped non-prod apps.

Reliability/security: zone redundancy in prod where supported, private endpoints/DNS, managed identity, least privilege, TLS 1.2+, encryption and environment isolation. Alert on queue age, DLQ and Salesforce budget. **Validation:** Bicep CLI 0.49.2 compiled the template successfully. No deployment, subscription-level validation or security review was performed.
