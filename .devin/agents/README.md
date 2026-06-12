# Agents Index

Custom subagent profiles for the `event_recommender` project.
These files are committed and available to the whole team.

## Layout

Each agent is a directory containing an `AGENT.md` file with YAML frontmatter
and a system prompt. The directory name becomes the subagent's identifier.

```
.devin/agents/
├── README.md                          ← this file
├── sdlc/AGENT.md                      ← Pipeline Orchestrator
├── plan-requirements/AGENT.md         ← PLAN phase
├── code-architect/AGENT.md            ← CODE Architecture phase
├── code-implement/AGENT.md            ← CODE Implement phase
├── test-qa/AGENT.md                   ← TEST phase
├── release-pr/AGENT.md                ← RELEASE phase
└── docker-springboot-tester/AGENT.md  ← Docker/Spring Boot validator
```

## How to invoke (Devin CLI)

Subagents in Devin CLI are **delegated to by the root agent**, not selected with
`@<name>` like in VS Code Copilot Agent mode. You ask Devin to use a specific
subagent and it spawns one via the `run_subagent` tool with that profile.

Examples of how to ask:
- "Start the SDLC pipeline" → Devin picks the `sdlc` subagent
- "Use the sdlc subagent to plan GitHub issue #5"
- "Run the plan-requirements agent on issue #5"
- "Hand this off to the code-implement agent"
- "Validate the Docker build with the docker-springboot-tester agent"

Subagent activation is driven by the `description` field in each `AGENT.md`'s
frontmatter — trigger phrases listed there help Devin select the right profile.

## Pipeline (Greenfield Feature path)

```
sdlc → plan-requirements → code-architect → code-implement → test-qa → release-pr
```

## Agent Catalogue

| Agent directory | Role |
|-----------------|------|
| `sdlc/` | Orchestrator — routes to correct path and tracks pipeline progress |
| `plan-requirements/` | Requirements Engineer — produces `plan.md` from a GitHub Issue |
| `code-architect/` | Architecture Governance — ADRs, system design, tech decisions |
| `code-implement/` | Dev Executor — writes production code following plan + ADRs |
| `test-qa/` | QA Engineer — test design, test generation, test execution |
| `release-pr/` | Release Agent — creates PR, computes SDLC score, closes ticket |
| `docker-springboot-tester/` | Docker + Spring Boot validation engineer |

## Artifacts (per ticket)

All artifacts are stored in `.stage/<TICKET-ID>/` (e.g., `.stage/GH-12/`):

| File | Created by |
|------|-----------|
| `plan.md` | `plan-requirements` |
| `plan-score.md` | `plan-requirements` |
| `arch-review.md` | `code-architect` |
| `arch-score.md` | `code-architect` |
| `implementationReport.md` | `code-implement` |
| `code-score.md` | `code-implement` |
| `testDesign.md` | `test-qa` |
| `testResults.md` | `test-qa` |
| `test-score.md` | `test-qa` |
| `score.md` | `release-pr` (aggregated) |
| `release-score.md` | `release-pr` |

Shared across all tickets:
- `.stage/docs/architecture.md` — system architecture (owned by `code-architect`)
- `docs/adr/ADR-NNNN-*.md` — Architecture Decision Records (owned by `code-architect`)

## Frontmatter Reference

Each `AGENT.md` uses the same YAML frontmatter as Devin skills:

```yaml
---
name: <agent-id>           # must match the directory name
description: |             # shown to the root agent for profile selection
  Trigger phrases and use cases for this subagent.
model: sonnet              # (optional) model override
allowed-tools:             # (optional) restrict tool access
  - read
  - grep
  - exec
permissions:               # (optional) per-tool allow/deny/ask overrides
  allow:
    - Exec(git diff)
  deny:
    - write
---

<system prompt body goes here>
```

See the Devin CLI docs at `extensibility/index` and `subagents` for the full
field reference and tool-permission grammar.

## VS Code Copilot Equivalents

If you also use VS Code Copilot Agent mode, the same agents (in the older
`@<name>` invocation format) live under `.github/agents/*.agent.md`. Keep the
two in sync if you make role changes.

## Notes / Caveats

- Devin's rules loader treats `AGENT.md` files as always-on rules when the
  agent accesses files in their directory. In normal use (delegating to a
  subagent without first opening its directory), this side effect doesn't
  fire — the subagent runs with the AGENT.md body as its system prompt
  exactly as intended. Avoid editing or browsing these files inside an
  active root session if you want the cleanest context.
