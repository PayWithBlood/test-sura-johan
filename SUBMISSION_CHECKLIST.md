# Submission checklist

Status for the current delivery. Review the privacy exception before submission.

## General
- [x] `SUBMISSION.md` contains candidate ID, repository URL, candidate-reported total hours, submission date, stack and instructions for the final SHA.
- [x] `AI_USAGE.md` completed honestly.
- [x] `docs/ASSUMPTIONS.md` lists design assumptions.
- [ ] No real personal data in repository/history: the candidate's email is included as ID at their request. This conflicts with the assessment's no-personal-data rule; confirm with recruiter or use an approved alternative if required.
- [x] Repository is public: https://github.com/PayWithBlood/test-sura-johan (checked without signing in). Push the final local commits before sending the link.

## Part A — Integration and data design
- [x] `docs/design/SOLUTION_DESIGN.md` has context/sequence diagrams, mapping, idempotency, ordering, failures, capacity, data and FR7.
- [x] `docs/adr/0001-salesforce-integration.md`
- [x] `docs/adr/0002-stack-and-store.md`
- [x] `docs/adr/0003-durable-acceptance.md`

## Part B — Integration component
- [x] `docs/api/webhooks.openapi.yaml` (validated)
- [x] `docs/api/events/premium-payment.schema.json` (compiled with Ajv)
- [x] `docs/api/README.md`
- [x] Quetzal MX and Andina CO HTTP endpoints and canonical mapping implemented.
- [x] Eight Maven tests passed, including provider vectors, idempotency, rejects and MockMvc endpoint tests.
- [x] `practical/README.md` records run steps and local-only stubs.

## Part C — Azure IaC and CI/CD
- [x] Bicep in `infra/main.bicep`; Bicep CLI 0.49.2 compilation passed; no deployment.
- [x] `docs/infra/AZURE_SERVICES.md` includes service choices and cost estimate.
- [x] GitHub Actions CI and manual dev→test→prod CD definitions present; hosted workflows not run.

## Part D — Operations
- [x] `docs/operations/OPERATIONS.md`
- [x] `docs/operations/INC-0412-RESPONSE.md`

## Part E — Code review
- [x] `pr-review/REVIEW.md` prioritizes findings and verdict.

## Part F — Responsible AI
- [x] `AI_USAGE.md`
- [x] `docs/ai/AI_GUIDELINES.md`
