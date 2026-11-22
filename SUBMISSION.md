# Submission

## Candidate
| Field | Value |
|---|---|
| Candidate ID | johan_12_27@hotmail.com |
| Repository URL | https://github.com/PayWithBlood/test-sura-johan |
| Final commit SHA | Use the SHA of the final local `HEAD` printed after this metadata commit; include it in the recruiter reply. |
| Date of submission | 21 Nov 2026, COT; prepared before the 23:59 deadline. Recruiter reply is still pending. |

## Time spent
| Part | Approximate time |
|---|---|
| A. Integration and data design | 1 h 20 min |
| B. Integration component | 1 h 20 min |
| C. Azure IaC and CI/CD | 40 min |
| D. Operations | 30 min |
| E. Code review | 30 min |
| F. Responsible AI | 20 min |
| **Total** | **5 hours (candidate-reported; per-part split reconstructed approximately after the fact)** |

## Stack
- Practical part: Java 21, Spring Boot 3.3.13, Maven, JUnit 5
- Infrastructure as code: Bicep CLI 0.49.2; compiled successfully, not deployed
- Pipeline: GitHub Actions; CI/CD definitions present, hosted workflows not run
- Local verification: 8 Maven tests passed; OpenAPI and JSON Schema validated; both Mermaid diagrams rendered.

## Where to start reading
1. `docs/design/SOLUTION_DESIGN.md`
2. `docs/adr/0001-salesforce-integration.md`
3. `practical/README.md`

## Known gaps and next steps
The practical app uses in-memory inbox and queue adapters only in its local profile. Production still needs Cosmos/Change Feed, Service Bus, Key Vault and Salesforce adapters, plus APIM policies, durable retry/replay and load/security checks. Bicep compiled locally but was not checked against a subscription or deployed. Confirm partial-refund and retention rules. The recruiter reply remains to be sent with this repository URL and the final HEAD SHA.
