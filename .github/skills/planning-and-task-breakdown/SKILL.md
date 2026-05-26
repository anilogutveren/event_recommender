---
name: planning-and-task-breakdown
description: >
  Decompose a feature spec into vertical implementation slices with explicit acceptance
  criteria and dependency graph. Use after spec is written, before implementation.
  Triggers: "break down tasks", "plan implementation", "task list", "what do I build first".
---

# Skill: Planning & Task Breakdown

## Before You Plan — Clarify First

Surface ambiguities before writing any task. Load `interview` skill. Conduct a structured interview — group questions into batches of 3-4, with multiple choice options:

| Question | Why It Matters |
|---|---|
| What user problem does this solve? | Ensures tasks trace to value |
| What are the success criteria (testable)? | Defines "done" |
| What is explicitly out of scope? | Prevents scope creep |
| Does this change any API contract? | Forces frontend/backend coordination |
| What does this depend on? What depends on this? | Surfaces blocking tasks |

## Overview

Converts a feature spec into an ordered list of vertical implementation slices.
Each slice compiles, tests pass, and the system is in a working state after each one.

## When to Use

- After `spec-driven-development` produces a spec
- When a task feels too large to start
- When you need to parallelize work
- Before creating GitHub issues for a feature

## Decomposition Rules

1. **Vertical slices** — each task delivers observable value (not "add database table", but "user can register").
2. **Dependency order** — schema → domain model → repository → service → controller → UI.
3. **Testable acceptance criteria** — every task has `[ ]` checkboxes that a test can verify.
4. **Size targets** — XS/S preferred; L/XL must be split.

## Task Size Guide

| Size | Files touched | Estimated time | Action |
|---|---|---|---|
| XS | 1 | < 15 min | Fine as-is |
| S | 1–2 | 15–60 min | Fine as-is |
| M | 3–5 | 1–3 hours | Usually fine |
| L | 5–8 | 3–8 hours | **Split** |
| XL | 8+ | > 1 day | **Always split** |

## Dependency Graph for Vue + Kotlin Feature

```
1. Database schema / migration
         ↓
2. Kotlin domain model (data class)
         ↓
3. Kotlin repository interface + implementation
         ↓
4. Kotlin service layer
         ↓
5. Kotlin controller + request/response DTOs
         ↓
6. OpenAPI spec update
         ↓
7. Vue API client (type-safe fetch wrapper)
         ↓
8. Vue composable (use-*.ts)
         ↓
9. Vue component (presentation layer)
         ↓
10. Vue page/view (container + routing)
         ↓
11. E2E test (Playwright critical path)
```

## Task Template

```markdown
### Task N: [Component Name]

**Size:** S | M
**Depends on:** Task N-1

**Files:**
- Create: `src/main/kotlin/[package]/UserService.kt`
- Create: `src/test/kotlin/[package]/UserServiceTest.kt`
- Modify: `src/main/kotlin/[package]/UserController.kt`

**Acceptance Criteria:**
- [ ] `UserService.createUser()` saves user to DB and returns saved entity
- [ ] `UserService.createUser()` throws `ConflictException` for duplicate email
- [ ] Unit tests cover both success and conflict cases

**Verification:**
```bash
./gradlew test --tests "UserServiceTest"
```
```

## Output Files

```
.ai/plans/
  plan.md       # full plan with all tasks
  todo.md       # current status tracker
```

## Example Plan for "User Registration" Feature

```markdown
# Plan: User Registration

## Overview
Allow users to create accounts with email + password.
Frontend: Vue registration form.
Backend: Kotlin REST endpoint with validation.

## Task 1: Database migration (XS)
- Create: `db/migrations/V001__create_users_table.sql`
- Creates: users table with id, email, password_hash, created_at
- Acceptance: migration runs cleanly on test DB

## Task 2: Kotlin domain model (XS)
- Create: `src/main/kotlin/domain/User.kt`
- `data class User(val id: UUID, val email: String, ...)`
- Acceptance: compiles, no external dependencies

## Task 3: Kotlin repository (S)
- Create: `UserRepository.kt` (interface + JPA/Exposed impl)
- Create: `UserRepositoryIntegrationTest.kt`
- Acceptance: findById, save, existsByEmail work against test DB

## Task 4: Kotlin service (S)
- Create: `UserService.kt`
- Create: `UserServiceTest.kt`
- Acceptance: createUser saves, throws ConflictException for duplicate email

## Task 5: Kotlin controller (S)
- Create: `UserController.kt`
- Create: `UserApiIntegrationTest.kt`
- Acceptance: POST /api/v1/users returns 201; 400 for invalid; 409 for duplicate

## Task 6: Vue API client (XS)
- Create: `src/api/user-api.ts`
- Type-safe wrapper for POST /api/v1/users

## Task 7: Vue composable (S)
- Create: `use-registration.ts` + `use-registration.spec.ts`
- Manages form state, validation, API call, success/error states

## Task 8: Vue registration form (M)
- Create: `RegisterForm.vue` + `RegisterForm.spec.ts`
- Fields: email, password, confirm password
- Shows validation errors inline
- Shows success state after submission

## Task 9: Vue page + routing (XS)
- Create: `RegisterView.vue`
- Add route `/register` to router

## Task 10: E2E test (S)
- Create: `e2e/user-registration.spec.ts`
- Happy path: register → redirect to login
- Error path: duplicate email → show error
```

## Scope Reduction Prohibition

Never silently reduce scope because a task "feels too complex". If the full scope cannot fit in the current planning horizon, **split the phase** — do not quietly omit features.

**Forbidden language in task definitions:**
- `"v1"`, `"simplified version"`, `"static for now"`, `"hardcoded for now"`
- `"future enhancement"`, `"placeholder"`, `"basic version"`, `"minimal implementation"`
- `"will be wired later"`, `"dynamic in future phase"`, `"skip for now"`

**When scope doesn't fit:** Return `PHASE SPLIT RECOMMENDED` and propose how to divide the work into natural sub-phases. Present the split to the user for a decision. Do not make the decision silently.

**Multi-source coverage audit:** Before finalizing a plan, verify that every source artifact is covered:
- [ ] Every success criterion from the spec has at least one task
- [ ] Every API contract change has a task
- [ ] Every UI change has a task
- [ ] Every migration or schema change has a task

If an item cannot be covered: surface it explicitly, do not hide it.

## Common Rationalizations

| Rationalization | Reality |
|---|---|
| "This is small enough to keep in one task" | If it touches 3+ files, split it. Reviewers and future-you will thank you. |
| "I'll figure out dependencies as I go" | Unordered tasks create circular blockers mid-sprint. Invest 10 minutes mapping the graph now. |
| "The acceptance criteria are obvious" | Write them anyway. "Obvious" criteria become disagreements during review. |
| "I'll skip E2E — we have unit tests" | Unit tests don't catch integration failures. One E2E test per critical path is the minimum. |
| "This L task is actually fine" | It's never fine. An L task means unclear requirements. Split until you understand it. |

## Verification Checklist

- [ ] Each task has explicit acceptance criteria with `[ ]` checkboxes
- [ ] Tasks are in dependency order (nothing skips a layer)
- [ ] All tasks are S or smaller (L/XL must be split)
- [ ] Verification commands included per task
- [ ] Plan saved to `.ai/plans/plan.md`
