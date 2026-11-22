# AI usage disclosure
AI assistants are permitted in this assessment and must be disclosed.

## Tools
| Tool | Where used | Purpose |
|---|---|---|
| OpenAI Codex (GPT-6 desktop) | Assessment review; design, ADR, API, Java/tests, infrastructure, operations, PR review and submission drafts | Read supplied scenario/reference material, draft deliverables and reason through edge cases. |

## Validation
Reviewed the scenario, provider specifications/test vectors, Salesforce model/limits, incident and PR. Ran `mvn -B test` in `practical/`: 8 tests passed, including MockMvc checks of both HTTP endpoints. Compiled `infra/main.bicep` with Bicep CLI 0.49.2: succeeded; nothing deployed. Both Mermaid diagrams rendered with Mermaid CLI. OpenAPI validation passed with Swagger Parser and JSON Schema compiled with Ajv 2020-12. These are syntax/contract checks, not load, security or Azure subscription tests. Candidate should review every file and explain it in the defense.

## Discarded or corrected
Did not conceal AI use because the assessment requires disclosure. Used exact decimal arithmetic rather than floating point; treated Andina retry IDs as attempt IDs and its v2.3 policy reference as untrusted input; excluded payer/token fields from canonical events. Fixed Mermaid and OpenAPI errors found by validators and added the HTTP controller plus local-only fake adapters after finding the first draft lacked routes.

## Data shared with AI tools
The fictional assessment repository was read, including fictional provider test secrets and synthetic payer examples. The candidate also supplied the email address recorded as Candidate ID in `SUBMISSION.md`. No other real credentials, real payer data or employer-confidential information were intentionally supplied. The email is retained only in this local draft; the repository has not been published or sent.
