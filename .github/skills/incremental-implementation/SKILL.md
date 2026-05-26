---
name: incremental-implementation
description: >
  Implement features as thin vertical slices: one change, one test, one commit.
  Use for any multi-file change to stay in control. Triggers: "implement",
  "build feature", "make changes", any implementation task.
---

# Skill: Incremental Implementation

## Core Rule

**Implement → Test → Verify → Commit → Next slice.**

Never batch multiple changes before testing. The system must compile and tests must pass
after every increment.

## The 5 Rules

**Rule 0 — Simplicity First:**
Ask "what is the simplest thing that could work?" before writing anything clever.

**Rule 1 — One Thing at a Time:**
Separate commits for: test addition, feature implementation, refactoring.
Never mix these in a single commit.

**Rule 2 — Always Compiling:**
The system must compile after every increment.
If it doesn't, fix it before moving forward.

**Rule 3 — Feature Flags for Incomplete Features:**
Merge incomplete work behind a flag. Never commit broken code to main.

**Rule 4 — Scope Discipline:**
Do NOT clean up adjacent code while implementing a feature.
```
NOTICED BUT NOT TOUCHING: [describe what you saw]
→ Use the `ask_user` tool (via `interview` skill) to ask whether to create a task for this.
```

**Rule 5 — Rollback-Friendly:**
Prefer additive changes over modifications. New file > modified file for large changes.

## Increment Pattern

```
Each increment:
  1. Write ONE failing test (RED)
  2. Write MINIMAL code to pass it (GREEN)
  3. Run test suite — all must pass
  4. Commit:
     git add <relevant files only>
     git commit -m "feat(scope): specific description"
  5. Move to next increment
```

## Slice Sizing

A good increment is:
- One logical step in the dependency chain
- Completable in < 1 hour
- Testable in isolation

If you feel the urge to change 5+ files at once — stop, decompose further.

## Example Sequence for "Add User Email Verification"

```
Increment 1: DB schema
  - Add verified_at column to users table
  - Migration test: column exists
  git commit -m "chore(db): add verified_at column to users"

Increment 2: Domain model
  - Add verifiedAt: Instant? to User data class
  - Update existing tests (no new functionality yet)
  git commit -m "feat(user): add verifiedAt field to User domain model"

Increment 3: Verification token service
  - Write failing test: generateToken returns unique token
  - Implement generateToken()
  - Write failing test: validateToken returns userId for valid token
  - Implement validateToken()
  git commit -m "feat(auth): add email verification token service"

Increment 4: API endpoint
  - Write failing integration test: POST /api/v1/auth/verify returns 200
  - Implement VerificationController.verifyEmail()
  git commit -m "feat(auth): add email verification endpoint"

Increment 5: Vue composable
  - Write failing test: useEmailVerification calls API on mounted
  - Implement useEmailVerification()
  git commit -m "feat(vue): add useEmailVerification composable"

Increment 6: Vue component
  - Write component test
  - Implement EmailVerificationView.vue
  git commit -m "feat(vue): add email verification view"
```

## What to Do When You Spot Related Issues

```
# While implementing UserService.createUser(), you notice the UserRepository
# has an N+1 query in getUsersWithOrders().

NOTICED BUT NOT TOUCHING:
  - UserRepository.getUsersWithOrders() has N+1 query pattern
  → Creating task: refactor(repo): fix N+1 in getUsersWithOrders
```

Create the task, continue the current increment. Do not derail.

## Commit Message During Implementation

Each commit should stand alone in `git log`:
```
feat(auth): verify email before allowing login
test(auth): add failing test for email verification gate
refactor(auth): extract token validation to VerificationService
```

## Deviation Rules

During implementation you will discover work that is not in the plan. Apply these rules automatically and track every deviation.

**Rule 1 — Auto-fix bugs:**
Trigger: Code doesn't work as intended (broken behavior, errors, wrong output, security vulnerabilities).
Fix inline → update/add tests → verify → continue → track as `[Rule 1] description`.

**Rule 2 — Auto-add missing critical functionality:**
Trigger: Code is missing essentials for correctness, security, or basic operation (missing error handling, no input validation, no null checks, unprotected routes).
*Critical = required for correct/secure/performant operation — not a "feature."*
Fix inline → track as `[Rule 2] description`.

**Rule 3 — Auto-fix blocking issues:**
Trigger: Something prevents completing the current task (broken import, missing dependency, wrong types, build config error).
Fix inline → track as `[Rule 3] description`.

**Rule 4 — Pause for architectural changes:**
Trigger: Fix requires a significant structural change (new DB table, switching library, breaking API change, new service layer).
Action: **STOP** → report what was found, proposed change, why, impact, alternatives. User decision required.

**Priority:** Rule 4 beats Rules 1–3. When in doubt, ask.

**Scope boundary:** Only auto-fix issues *directly caused by the current task*. Pre-existing warnings in unrelated files → log as `NOTICED BUT NOT TOUCHING`, do not fix.

**Fix attempt limit:** After 3 auto-fix attempts on a single issue → document as deferred, move on.

## Common Rationalizations

| Rationalization | Reality |
|---|---|
| "I'll test it all at the end" | Bugs compound. A bug in Increment 1 makes Increments 2–5 wrong. Test each slice. |
| "It's faster to do it all at once" | It *feels* faster until something breaks and you can't find which of 500 changed lines caused it. |
| "These changes are too small to commit separately" | Small commits are free. Large commits hide bugs and make rollbacks painful. |
| "I'll add the feature flag later" | If the feature isn't complete, it shouldn't be user-visible. Add the flag now. |
| "This refactor is small enough to include" | Refactors mixed with features make both harder to review and debug. Separate them. |

## Verification Checklist

- [ ] System compiles after each increment
- [ ] All tests pass after each increment
- [ ] Each increment has exactly one commit
- [ ] No adjacent code touched (scope discipline)
- [ ] Noticed-but-not-touching items tracked as new tasks
- [ ] Feature flags used for partially-implemented features on main
