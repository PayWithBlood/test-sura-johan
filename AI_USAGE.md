# AI usage disclosure
AI assistants are permitted in this assessment and must be disclosed.

## Tools
| Tool | Where used | Purpose |
|---|---|---|
| OpenAI Codex (GPT-6 desktop) | Assessment review; design, ADR, API, Java/tests, infrastructure, operations, PR review and submission drafts | Read supplied scenario/reference material, draft deliverables and reason through edge cases. |

## Validation
Reviewed the scenario, provider specifications/test vectors, Salesforce model/limits, incident and PR. Ran `mvn -B test` in `practical/`: 8 tests passed, including MockMvc checks of both HTTP endpoints. Compiled `infra/main.bicep` with Bicep CLI 0.49.2: succeeded; nothing deployed. Both Mermaid diagrams rendered with Mermaid CLI. OpenAPI validation passed with Swagger Parser and JSON Schema compiled with Ajv 2020-12. These are syntax/contract checks, not load, security or Azure subscription tests. Candidate should review every file and explain it in the defense.

