# AI usage disclosure
AI assistants are allowed and must be disclosed.

## Tools
| Tool | Where used | Purpose |
|---|---|---|
| OpenAI Codex (GPT-6 desktop) | Assessment review; design, ADR, API, Java/tests, infrastructure, operations, PR review drafts | Read supplied scenario/reference material, draft deliverables and reason through edge cases. |

## Validation
Reviewed scenario, provider specs/vectors, Salesforce model/limits, incident and PR. Test cases target supplied vectors and failure paths. Record actual test execution result before submission. Capacity numbers are calculations, not load-test findings. Candidate must review all changes and understand them before defense.

## Discarded or corrected
Did not conceal AI use because the assessment requires disclosure. Chose durable inbox-before-2xx over an in-memory executor; exact decimal arithmetic over floating point; Andina transition identity over retry attempt ID; strict policy validation over guessing; and excluded payer/token data from canonical output.

## Data shared
The fictional assessment repository was read, including fictional provider test secrets and synthetic payer examples. No real credentials, real personal data, or employer-confidential information were intentionally supplied. Test secrets are sandbox fixtures, not production secrets.
