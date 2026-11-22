# Infrastructure as code
Bicep source is `main.bicep`; parameters cover environment (dev/test/prod), region, image and country list. It defines Container Apps, Cosmos DB, Service Bus Premium work queue/session/DLQ and event topic, Key Vault with RBAC, managed identity assignments, Log Analytics, App Insights and a DLQ metric alert.

**Validation:** Bicep CLI 0.49.2 compiled `infra/main.bicep` successfully to a temporary ARM template. The compiler was downloaded into the workspace's `work/tooling`; it was not installed globally. No deployment or subscription-level what-if was run. The template still needs review of role scopes, private endpoints/DNS, alert action groups, separate identities and production network access before deployment. No secrets or provider credentials are parameters.
