# event_recommender — Agentic SDLC

This project uses a **Jira-free, GitHub-Issues-driven agentic SDLC** modeled after the `ai4swe-hub-sdlc-windsurf` pipeline.
All agents live in `.github/agents/` and are invoked with `@<name>` in VS Code Copilot Agent mode.

---

## How to Start

1. Open VS Code Copilot Chat in **Agent mode**
2. Type `@sdlc` to invoke the orchestrator
3. The orchestrator will ask for your GitHub Issue number and route you to the correct pipeline path
4. Follow the human-in-the-loop gates at each phase — nothing auto-proceeds

---

## Pipeline Paths

| # | Path | Chain |
|---|------|-------|
| 01 | Greenfield Feature | `@plan-requirements` → `@code-architect` → `@code-implement` → `@test-qa` → `@release-pr` |
| 02 | Bug Fix | `@plan-requirements` → `@code-architect` [optional] → `@code-implement` → `@test-qa` → `@release-pr` |
| 03 | PoC / Spike | `@code-implement` → `@test-qa` → `@release-pr` |
| 04 | Hotfix | `@plan-requirements` [lite] → `@code-implement` → `@test-qa` → `@release-pr` |

---

## Agent Catalogue

| Agent | Role | Persona |
|-------|------|---------|
| `@sdlc` | Pipeline Orchestrator | Routes tickets to the correct path, tracks progress |
| `@plan-requirements` | PLAN Phase | Requirements Engineer — crystal-clear, testable requirements |
| `@code-architect` | CODE Phase (Architecture) | Architecture Governance — ADRs, tech decisions, system design |
| `@code-implement` | CODE Phase (Implement) | Dev Executor — production code following plan + architecture |
| `@test-qa` | TEST Phase | QA Engineer — test design, test generation, test execution |
| `@release-pr` | RELEASE Phase | Release Agent — PR creation, SDLC score, ticket closure |

---

## Artifact Structure

Every ticket produces a `.stage/<TICKET-ID>/` folder (e.g., `.stage/GH-12/`):

```
.stage/
  GH-12/
    plan.md                  # Requirements & acceptance criteria
    plan-score.md            # PLAN phase evaluation score
    arch-review.md           # Architecture review output
    arch-score.md            # ARCHITECTURE phase score
    implementationReport.md  # What was built and why
    code-score.md            # CODE phase evaluation score
    testDesign.md            # Systematic test cases
    testTraceability.md      # Requirement ↔ test mapping
    testResults.md           # Test execution results
    test-score.md            # TEST phase evaluation score
    score.md                 # Aggregated SDLC scorecard
    release-score.md         # RELEASE phase evaluation score
  docs/
    architecture.md          # Shared system architecture (all tickets)
docs/
  adr/
    ADR-0001-*.md            # Architecture Decision Records (permanent)
```

---

## SDLC Progress Tracker

Each agent updates and displays this block at every handoff:

```
### SDLC Progress — <TICKET-ID>
- [ ] PLAN Phase
- [ ] CODE Phase (Architecture)
- [ ] CODE Phase (Implement)
- [ ] TEST Phase
- [ ] RELEASE Phase
```

---

## Core Principles (inherited from ai4swe-hub-sdlc-windsurf)

- **Human-in-the-loop** — no agent auto-proceeds to the next phase
- **Ticket-bound artifacts** — every file is namespaced under `.stage/<TICKET-ID>/`
- **Phase evaluation is mandatory** — every agent self-scores before the confirmation gate
- **Security is non-negotiable** — OWASP Top 10 check on every implementation
- **Architecture governs code** — no implementation deviates from documented ADRs without a new ADR
