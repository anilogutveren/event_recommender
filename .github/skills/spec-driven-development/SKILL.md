---
name: spec-driven-development
description: >
  Write a feature specification before writing any code. Use for any task > 30 minutes,
  any multi-file change, or when requirements are vague. Triggers: "new feature",
  "requirements unclear", "what should I build", "define scope".
---

# Skill: Spec-Driven Development

## When to Use

- Any feature that will take > 30 minutes
- Any multi-file change
- When requirements are vague or ambiguous
- Before starting a new component, service, or module

## When NOT to Use

- Typo fixes
- Single-line config changes
- Clear, fully-scoped, < 10 minute tasks

## 4-Phase Process

```
SPECIFY → PLAN → TASKS → IMPLEMENT
```

### Phase 1: SPECIFY — Write the Spec

Before touching code, answer these 6 questions in `.ai/specs/SPEC.md`:

```markdown
# SPEC: [Feature Name]

## Objective
What user problem does this solve? One sentence.

## Success Criteria
- [ ] User can [specific observable behavior]
- [ ] [Measurable criterion]
(These become your acceptance tests)

## Scope
### Included
- [explicitly in scope]

### Excluded (Not Doing)
- [explicitly out of scope — as important as what IS in scope]

## Technical Approach
Which components/services are affected? Key design decisions.

## Testing Strategy
Unit tests: [what to unit test]
Integration tests: [what to integration test]
E2E: [critical paths to test with Playwright]

## Assumptions
- [Assumption 1 — state it so it can be challenged]
- [Assumption 2]
```

**Before writing the spec, surface assumptions:**
> "ASSUMPTIONS I'M MAKING: 1. [assumption] → Correct me now if wrong."

### Phase 2: PLAN — Architecture

Map out the affected code:
- Files to create
- Files to modify
- Data flow through the system
- API contract changes (update OpenAPI spec first)

### Phase 3: TASKS — Break It Down

Decompose into vertical slices with acceptance criteria.
See `planning-and-task-breakdown` skill for detail.

### Phase 4: IMPLEMENT

Execute tasks using `incremental-implementation` + `test-driven-development` skills.

## Common Spec Failures to Avoid

| Failure | Fix |
|---|---|
| Vague success criteria ("it works") | Rewrite as testable: "User sees X when Y" |
| No Not-Doing list | Explicitly list excluded scope |
| Hidden assumptions | Surface them before writing code |
| UI-only spec | Include API contract and validation rules |
| No error scenarios | Add: "what happens when X fails?" |

## Spec Template for Vue + Kotlin Feature

```markdown
# SPEC: [Feature Name]

## Objective
[User-facing problem this solves, one sentence]

## User Story
When [situation], I want to [action], so I can [outcome].

## Acceptance Criteria
- [ ] Vue: Component renders [X] when [condition]
- [ ] Vue: Form validates [field] before submission
- [ ] API: POST /api/v1/[resource] returns 201 with [shape]
- [ ] API: Returns 400 with field errors for invalid input
- [ ] API: Returns 409 for duplicate [unique field]

## Out of Scope
- [Do NOT build this yet]

## API Contract
### POST /api/v1/[resource]
Request: { ... }
Response 201: { ... }
Response 400: { "error": { "code": "VALIDATION_ERROR", ... } }

## Vue Components
- [ComponentName]: [purpose]
- [composableName]: [what it manages]

## Pinia Stores
- [storeName]: [what it holds]

## Technical Assumptions
1. [Assumption — challenge before implementing]
```

## Common Rationalizations

| Rationalization | Reality |
|---|---|
| "This is simple, I don't need a spec" | Simple tasks don't need *long* specs, but they still need acceptance criteria. A two-line spec is fine. |
| "I'll write the spec after I code it" | That's documentation, not specification. The spec's value is in forcing clarity *before* code. |
| "The spec will slow us down" | A 15-minute spec prevents hours of rework. Waterfall in 15 minutes beats debugging in 15 hours. |
| "Requirements will change anyway" | That's why the spec is a living document. An outdated spec is still better than no spec. |
| "We all know what needs to be built" | Even clear requests have implicit assumptions. The spec surfaces those assumptions before they become bugs. |

## Verification Checklist

- [ ] Spec written BEFORE any code
- [ ] Success criteria are testable (not subjective)
- [ ] Not-Doing list explicitly stated
- [ ] Assumptions surfaced and confirmed
- [ ] API contract defined (request/response shapes, status codes)
- [ ] Vue component list identified
- [ ] Testing strategy defined (unit/integration/E2E boundaries)
