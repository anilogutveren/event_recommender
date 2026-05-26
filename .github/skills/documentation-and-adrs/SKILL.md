---
name: documentation-and-adrs
description: >
  Technical documentation standards and Architectural Decision Record (ADR) workflow.
  Use when making significant technical decisions, documenting APIs, or setting up
  project documentation. Triggers: "ADR", "document this decision", "architecture decision",
  "write documentation", "why did we choose".
---

# Skill: Documentation & ADRs

## Principle: Document the Why

Code shows **what** and **how**. Documentation explains **why** decisions were made.
Future developers (including you in 6 months) need to understand the reasoning, not just the result.

## Architectural Decision Records (ADRs)

### When to Write an ADR

Write an ADR for any decision that:
- Chooses between two viable technology options
- Deviates from an established pattern in this codebase
- Is hard or costly to reverse
- Affects a public API contract
- Multiple team members need to understand and align on

### ADR Template

```markdown
# ADR-NNN: [Short, imperative decision title]

**Status:** PROPOSED | ACCEPTED | SUPERSEDED | DEPRECATED
**Date:** YYYY-MM-DD
**Supersedes:** ADR-NNN (only if replacing a previous decision)

## Context

[2-4 sentences: the situation, constraints, and why a decision was needed now.
What forces are at play? What problem are we solving?]

## Decision

[1-3 sentences: the decision, stated clearly and unambiguously.
Start with "We will..." or "We have decided to..."]

## Alternatives Considered

| Option | Pros | Cons | Why Not Chosen |
|--------|------|------|----------------|
| [Option A] | [benefit] | [cost] | [reason rejected] |
| [Option B] | ... | ... | ... |
| Chosen: [Option C] | [benefit] | [cost/trade-off] | Chosen |

## Consequences

### Positive
- [concrete benefit 1]
- [concrete benefit 2]

### Negative / Trade-offs
- [concrete cost or risk]
- [what we give up]

### Neutral
- [things that change but are neither good nor bad]

## Implementation Notes
[Optional: specific things to watch out for, first steps, links to relevant code]
```

### ADR File Naming

```
docs/decisions/
  ADR-001-use-pinia-for-state-management.md
  ADR-002-ktor-over-spring-boot.md
  ADR-003-jwt-refresh-token-strategy.md
  ADR-004-testcontainers-for-integration-tests.md
```

### ADR Lifecycle Rules

- **Never delete** old ADRs — they are historical record.
- When superseding: update old ADR status to `SUPERSEDED by ADR-NNN`, create new ADR.
- Accepted ADRs are binding until superseded.
- Proposed ADRs need team review before becoming Accepted.

## README Standards

Every service, package, or module needs a README with:

```markdown
# [Service Name]

[One sentence: what this service does]

## Running Locally

\`\`\`bash
# Prerequisites: Docker, Java 21, Node 20
./gradlew bootRun
# or
npm run dev
\`\`\`

## Running Tests

\`\`\`bash
./gradlew test
npm run test
\`\`\`

## Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `DB_URL` | PostgreSQL connection string | Yes |
| `JWT_SECRET` | JWT signing secret (min 32 chars) | Yes |
| `EMAIL_API_KEY` | Email provider API key | Yes |

## API Documentation

OpenAPI spec: `docs/api/openapi.yaml`
Hosted docs: `http://localhost:8080/swagger-ui.html`

## Architecture

[Link to relevant ADRs or architecture diagrams]
```

## Code Comment Standards

```kotlin
// ✅ Good — explains WHY
// Delay retry with exponential backoff to avoid thundering herd
// against the payment provider. Capped at 30s per their SLA.
delay(min(baseDelay * 2.0.pow(retries), MAX_DELAY_MS.toDouble()).toLong())

// ❌ Bad — explains WHAT (visible from the code)
// Multiply base delay by 2 to the power of retries
delay(baseDelay * 2.0.pow(retries).toLong())
```

```ts
// ✅ Good — explains non-obvious behavior
// We intentionally don't await this — the welcome email is
// best-effort and should not block the registration response.
void emailService.sendWelcomeEmail(user)

// ❌ Bad — restates the code
// Call email service to send welcome email
emailService.sendWelcomeEmail(user)
```

## Changelog

Maintain `CHANGELOG.md` at project root:

```markdown
## [Unreleased]

### Added
- User profile editing with avatar upload (#142)

### Changed  
- Improved error messages for validation failures (#138)

### Fixed
- Null pointer in refresh token rotation (#145)

### Security
- Added rate limiting to all auth endpoints (#141)

## [1.2.0] — 2026-03-01

### Added
- Email verification flow
```

## OpenAPI Spec Workflow

1. Update `docs/api/openapi.yaml` **before** implementing the endpoint (contract-first).
2. Include examples for all request and response bodies.
3. Document all possible response codes (2xx, 4xx, 5xx).
4. Add the spec update to the same commit as the implementation.

## Architecture Decision Trees

Use these when writing ADRs for common decisions in this stack.

### Frontend State

```
Single component, no sharing?         → local ref + composable
Shared across ≥2 components?          → lift to parent or composable
Global / persisted across routes?     → Pinia store
Real-time / WebSocket data?           → Pinia store + WebSocket composable
```

### Backend Framework

```
Simple CRUD, fast delivery?           → Spring Boot MVC
High async throughput?                → Ktor (coroutines-native)
Complex business domain?              → Hexagonal architecture + domain layer
Event-driven between services?        → Add Kafka at service boundary
```

### Database

```
Relational data, joins, transactions? → PostgreSQL
Session / cache data?                 → Redis
Full-text search?                     → PostgreSQL pg_trgm or Elasticsearch
Append-only event log?                → PostgreSQL (append-only table)
```

## Architecture Anti-Patterns

| ❌ Anti-Pattern | Why It Hurts | Fix |
|---|---|---|
| Anemic domain model | Logic scattered in services, no single source of truth | Domain model with behavior |
| God service | Single class does everything, grows unbounded | Split by bounded context |
| Business logic in controllers | Untestable, violates SRP | Move to service layer |
| Business logic in repositories | Leaks domain concerns into data layer | Service owns decisions |
| Frontend calls N APIs per render | N+1 at API level, slow UX | BFF or aggregate endpoint |
| Shared DB between services | Tight coupling, change ripples everywhere | Each service owns its schema |

## Architecture Governance

Use these commands to keep the architecture honest as the codebase grows.

### `/arch decide` — Record a new ADR

Run when making a significant technical choice. Always document alternatives considered and why they were rejected. Use the ADR template above.

**Trigger automatically if:**
- Adding a new library or framework
- Changing the data model in a non-trivial way
- Deviating from an existing pattern in the codebase
- Making a decision that would be hard or expensive to reverse

### `/arch review` — Check code against architectural boundaries

Verify that new code respects the boundaries defined in existing ADRs:
- No business logic leaking into controllers
- No direct DB access from components
- Layer boundaries respected (controller → service → repository)
- ADR constraints honored (e.g., if ADR-002 says "use Ktor", no Spring Boot imports)

### `/arch evolve` — Detect architectural drift

Run periodically (or before major releases) to surface places where the codebase has drifted from documented decisions:

```bash
# Signals to scan for
grep -r "TODO: fix this later" src/
grep -r "// workaround" src/
git log --oneline --all | grep -i "quick fix\|temp\|hack"
```

For each drift found: either update the ADR to reflect the new reality, or create a refactoring task to restore alignment.

### Auto-Detect: When No Docs Exist

If the `docs/decisions/` folder is empty or missing, run `/arch decide` for at least these foundational choices before any other work:
1. Frontend framework choice
2. Backend framework choice
3. Database choice
4. Authentication strategy

Architecture governance is meaningless without a baseline.

## Verification Checklist

- [ ] ADR written for significant technical decisions
- [ ] ADR has all required sections (Context, Decision, Alternatives, Consequences)
- [ ] Old ADRs updated when superseded (not deleted)
- [ ] README up to date with correct run/test instructions
- [ ] OpenAPI spec updated alongside API changes
- [ ] Comments explain WHY, not WHAT
