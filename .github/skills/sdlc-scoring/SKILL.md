---
name: sdlc-scoring
description: >
  Scoring rubric for all SDLC phases in the event_recommender pipeline.
  Load this skill before computing a phase score to ensure consistent,
  criteria-based evaluation across PLAN, ARCHITECTURE, CODE, and TEST phases.
---

# SDLC Scoring Rubric — event_recommender

Load this skill before writing any `*-score.md` file. Score each phase on a **1–10 scale** using the criteria below. Be honest — scores feed the overall SDLC scorecard visible to reviewers.

---

## How to Score

1. Read the phase's output artifact (e.g., `plan.md`, `implementationReport.md`)
2. Score each criterion on its individual scale
3. Compute the weighted average
4. Write a 2-sentence **justification** for the score
5. List **one concrete improvement** the next phase should address

---

## PLAN Phase (`plan-score.md`)

| # | Criterion | Weight | 10 = … | 5 = … | 1 = … |
|---|-----------|--------|--------|--------|--------|
| 1 | Acceptance Criteria quality | 30% | All AC in GWT format, unambiguous, independently testable | AC present but partially ambiguous | No AC or untestable statements |
| 2 | Scope clarity | 20% | Explicit in-scope / out-of-scope list | Scope implied but not stated | Scope unknown |
| 3 | Non-functional requirements | 20% | Latency, security, and data constraints documented | One NFR present | No NFRs |
| 4 | User story quality | 15% | Clear persona, capability, and business benefit | Story present but benefit missing | No user story |
| 5 | Risk / open questions | 15% | All open questions resolved or escalated | Some open questions noted | No risks considered |

**Formula:** `(C1×0.30) + (C2×0.20) + (C3×0.20) + (C4×0.15) + (C5×0.15)`

---

## ARCHITECTURE Phase (`arch-score.md`)

| # | Criterion | Weight | 10 = … | 5 = … | 1 = … |
|---|-----------|--------|--------|--------|--------|
| 1 | ADR completeness | 30% | Context, decision, alternatives, consequences all present | Decision documented, no alternatives | No ADR written |
| 2 | Layer boundary compliance | 25% | New components placed correctly; no layer violations | Minor violation noted but not fixed | Multiple layer violations |
| 3 | Scalability / NFR alignment | 20% | Architecture explicitly addresses NFRs from plan | Partial alignment | NFRs ignored |
| 4 | Technology fit | 15% | Tech choice fits the stack (Kotlin/Spring Boot/ES) | Acceptable but suboptimal | Introduces new unjustified tech |
| 5 | Testability | 10% | DI and interfaces designed for easy mocking | Partially testable | Tightly coupled, untestable |

**Formula:** `(C1×0.30) + (C2×0.25) + (C3×0.20) + (C4×0.15) + (C5×0.10)`

---

## CODE Phase (`code-score.md`)

| # | Criterion | Weight | 10 = … | 5 = … | 1 = … |
|---|-----------|--------|--------|--------|--------|
| 1 | OWASP security (load `owasp-security` skill) | 25% | Zero open OWASP checklist items | 1–2 minor items | Any quick-fail item present |
| 2 | Kotlin conventions (load `kotlin-conventions` skill) | 20% | All conventions followed | Minor deviations | Widespread violations |
| 3 | Plan alignment | 20% | All AC from `plan.md` are implemented | ≥80% AC implemented | <80% AC implemented |
| 4 | Code quality | 20% | Functions ≤50 lines, ≤4 nesting, no hardcoded secrets | Minor quality issues | Major quality issues |
| 5 | Implementation report | 15% | Complete report: files changed, AC coverage, known gaps | Partial report | No report |

**Formula:** `(C1×0.25) + (C2×0.20) + (C3×0.20) + (C4×0.20) + (C5×0.15)`

> **Auto-fail**: Any OWASP quick-fail condition caps the CODE score at **5/10** regardless of other criteria.

---

## TEST Phase (`test-score.md`)

| # | Criterion | Weight | 10 = … | 5 = … | 1 = … |
|---|-----------|--------|--------|--------|--------|
| 1 | AC traceability | 30% | Every AC has ≥1 passing test with `// AC:` tag | ≥70% AC covered | <50% AC covered |
| 2 | Edge case coverage | 25% | All mandatory edge cases from `test-patterns` skill covered | Some edge cases covered | No edge cases |
| 3 | Test quality | 20% | Clear GWT structure, correct Mockk usage, descriptive names | Readable but minor issues | Unclear tests, wrong tools |
| 4 | Test results | 15% | All tests pass; no flaky tests | ≥90% pass rate | Tests fail or don't run |
| 5 | Test design doc | 10% | `testDesign.md` + `testTraceability.md` present and complete | Partial docs | No docs |

**Formula:** `(C1×0.30) + (C2×0.25) + (C3×0.20) + (C4×0.15) + (C5×0.10)`

---

## RELEASE Phase (`release-score.md`)

| # | Criterion | Weight | 10 = … | 5 = … | 1 = … |
|---|-----------|--------|--------|--------|--------|
| 1 | PR quality | 30% | Complete description, linked issue, all artifact links | Partial description | No description |
| 2 | Artifact completeness | 25% | All 5 stage artifacts present and non-empty | 3–4 artifacts present | <3 artifacts |
| 3 | SDLC score quality | 20% | Overall ≥8/10; all phases scored | Overall 6–7/10 | Overall <6/10 |
| 4 | Branch convention | 15% | Follows `<type>/GH-<N>-<description>` | Minor deviation | Wrong convention |
| 5 | Changelog / release notes | 10% | Clear summary of what changed and why | Present but vague | Absent |

**Formula:** `(C1×0.30) + (C2×0.25) + (C3×0.20) + (C4×0.15) + (C5×0.10)`

---

## Score Interpretation

| Range | Label | Action |
|-------|-------|--------|
| 9–10 | Excellent | Proceed to next phase |
| 7–8 | Good | Proceed; note improvement for next ticket |
| 5–6 | Acceptable | Proceed with documented gaps; revisit in next ticket |
| 3–4 | Needs Work | **Do not proceed** — return to current phase and address gaps |
| 1–2 | Unacceptable | **Block** — restart current phase |

---

## Score File Template

Save as `.stage/<TICKET-ID>/<phase>-score.md`:

```markdown
# <TICKET-ID> — <Phase> Score

**Date:** YYYY-MM-DD
**Phase:** <PLAN | ARCHITECTURE | CODE | TEST | RELEASE>
**Score:** X.X / 10

## Criteria Breakdown

| # | Criterion | Raw Score | Weight | Weighted |
|---|-----------|-----------|--------|---------|
| 1 | ... | /10 | X% | X.X |
| 2 | ... | /10 | X% | X.X |
| **Total** | | | | **X.X** |

## Justification
<2 sentences explaining the score.>

## Improvement for Next Phase
<One concrete, actionable item.>
```
