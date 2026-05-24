# Agents Index

Shared SDLC agents for the `event_recommender` project.
These files are committed and available to the whole team.
Invoke each agent with `@<name>` in VS Code Copilot Agent mode.

## Pipeline (Greenfield Feature path)

```
@sdlc  →  @plan-requirements  →  @code-architect  →  @code-implement  →  @test-qa  →  @release-pr
```

## Agent Catalogue

| Agent file | Invoke with | Role |
|------------|-------------|------|
| `sdlc.agent.md` | `@sdlc` | Orchestrator — routes to correct path and tracks pipeline progress |
| `plan-requirements.agent.md` | `@plan-requirements` | Requirements Engineer — produces `plan.md` from a GitHub Issue |
| `code-architect.agent.md` | `@code-architect` | Architecture Governance — ADRs, system design, tech decisions |
| `code-implement.agent.md` | `@code-implement` | Dev Executor — writes production code following plan + ADRs |
| `test-qa.agent.md` | `@test-qa` | QA Engineer — test design, test generation, test execution |
| `release-pr.agent.md` | `@release-pr` | Release Agent — creates PR, computes SDLC score, closes ticket |

## Artifacts (per ticket)

All artifacts are stored in `.stage/<TICKET-ID>/` (e.g., `.stage/GH-12/`):

| File | Created by |
|------|-----------|
| `plan.md` | `@plan-requirements` |
| `plan-score.md` | `@plan-requirements` |
| `arch-review.md` | `@code-architect` |
| `arch-score.md` | `@code-architect` |
| `implementationReport.md` | `@code-implement` |
| `code-score.md` | `@code-implement` |
| `testDesign.md` | `@test-qa` |
| `testResults.md` | `@test-qa` |
| `test-score.md` | `@test-qa` |
| `score.md` | `@release-pr` (aggregated) |
| `release-score.md` | `@release-pr` |

Shared across all tickets:
- `.stage/docs/architecture.md` — system architecture (owned by `@code-architect`)
- `docs/adr/ADR-NNNN-*.md` — Architecture Decision Records (owned by `@code-architect`)
