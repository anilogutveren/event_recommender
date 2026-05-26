---
name: code-simplification
description: >
  Simplifies code for clarity without changing behavior. Use when refactoring code
  after a feature is working, when code review flags readability issues, or when
  you encounter deeply nested logic, long functions, or unclear names.
  Triggers: "simplify", "refactor", "cleanup", "too complex", "hard to read".
---

# Skill: Code Simplification

## Overview

Simplify code by reducing complexity while preserving exact behavior. The goal is not fewer lines — it's code that is easier to read, understand, modify, and debug. Every simplification must pass this test: **"Would a new team member understand this faster than the original?"**

## When to Use

- After a feature is working and tests pass, but the implementation feels heavier than it needs to be
- During code review when readability or complexity issues are flagged
- When you encounter deeply nested logic, long functions, or unclear names
- When refactoring code written under time pressure
- After merging changes that introduced duplication or inconsistency

**When NOT to use:**

- Code is already clean and readable — don't simplify for the sake of it
- You don't understand what the code does yet — comprehend before you simplify
- The code is performance-critical and the "simpler" version would be measurably slower
- You're about to rewrite the module entirely — simplifying throwaway code wastes effort

## The Five Principles

### 1. Preserve Behavior Exactly

Don't change what the code does — only how it expresses it. All inputs, outputs, side effects, error behavior, and edge cases must remain identical.

```
ASK BEFORE EVERY CHANGE:
→ Does this produce the same output for every input?
→ Does this maintain the same error behavior?
→ Does this preserve the same side effects and ordering?
→ Do all existing tests still pass without modification?
```

### 2. Follow Project Conventions

Simplification means making code more consistent with the codebase, not imposing external preferences. Before simplifying: read neighboring code, understand naming conventions, match the project's style.

Simplification that breaks project consistency is not simplification — it's churn.

### 3. Prefer Clarity Over Cleverness

Explicit code is better than compact code when the compact version requires a mental pause to parse.

```typescript
// UNCLEAR: Dense ternary chain
const label = isNew ? 'New' : isUpdated ? 'Updated' : isArchived ? 'Archived' : 'Active';

// CLEAR: Readable guard clauses
function getStatusLabel(item: Item): string {
  if (item.isNew) return 'New';
  if (item.isUpdated) return 'Updated';
  if (item.isArchived) return 'Archived';
  return 'Active';
}
```

### 4. Maintain Balance

Simplification has a failure mode: over-simplification. Watch for:

- **Inlining too aggressively** — removing a helper that gave a concept a name
- **Combining unrelated logic** — two simple functions merged into one complex one
- **Removing purposeful abstraction** — some abstractions exist for testability or extensibility
- **Optimizing for line count** — fewer lines is not the goal; easier comprehension is

### 5. Scope to What Changed

Default to simplifying recently modified code. Avoid drive-by refactors of unrelated code. Unscoped simplification creates diff noise and risks regressions.

## The Simplification Process

### Step 1: Understand Before Touching (Chesterton's Fence)

Before changing or removing anything, understand why it exists. If you see something that looks unnecessary — don't remove it until you know why it was added.

```
BEFORE SIMPLIFYING, ANSWER:
- What is this code's responsibility?
- What calls it? What does it call?
- What are the edge cases and error paths?
- Are there tests that define the expected behavior?
- Why might it have been written this way? (Performance? Platform constraint? Historical reason?)
- Check git blame: what was the original context?
```

If you can't answer these, read more context first.

### Step 2: Identify Simplification Opportunities

**Structural complexity:**

| Pattern | Signal | Simplification |
|---------|--------|----------------|
| Deep nesting (3+ levels) | Hard to follow control flow | Extract to guard clauses or helper functions |
| Long functions (50+ lines) | Multiple responsibilities | Split into focused functions |
| Nested ternaries | Requires mental stack to parse | Replace with if/else or lookup maps |
| Boolean parameter flags | `doThing(true, false, true)` | Replace with options object or separate functions |
| Repeated conditionals | Same `if` check in multiple places | Extract to a well-named predicate |

**Naming and readability:**

| Pattern | Signal | Simplification |
|---------|--------|----------------|
| Generic names | `data`, `result`, `temp`, `val` | Rename to describe content: `userProfile`, `validationErrors` |
| Abbreviated names | `usr`, `cfg`, `btn` | Use full words unless the abbreviation is universal (`id`, `url`) |
| Misleading names | `get` function that also mutates | Rename to reflect actual behavior |
| Comments explaining "what" | `// increment counter` above `count++` | Delete — the code is clear enough |
| Comments explaining "why" | `// Retry because the API is flaky` | Keep — these carry intent the code can't express |

**Redundancy:**

| Pattern | Signal | Simplification |
|---------|--------|----------------|
| Duplicated logic | Same 5+ lines in multiple places | Extract to a shared function |
| Dead code | Unreachable branches, unused variables | Remove (after confirming it's truly dead) |
| Unnecessary abstractions | Wrapper that adds no value | Inline the wrapper |
| Over-engineered patterns | Factory-for-a-factory | Replace with the simple direct approach |

### Step 3: Apply Changes Incrementally

Make one simplification at a time. Run tests after each change. **Submit refactoring changes separately from feature or bug fix changes.**

```
FOR EACH SIMPLIFICATION:
1. Make the change
2. Run the test suite
3. Tests pass → continue or commit
4. Tests fail → revert and reconsider
```

**The Rule of 500:** If a refactoring would touch more than 500 lines, invest in automation (codemods, sed, AST transforms) rather than making changes by hand.

### Step 4: Verify the Result

```
COMPARE BEFORE AND AFTER:
- Is the simplified version genuinely easier to understand?
- Did you introduce any new patterns inconsistent with the codebase?
- Is the diff clean and reviewable?
- Would a teammate approve this change?
```

If the "simplified" version is harder to understand or review, revert.

## TypeScript / Kotlin Quick Reference

```typescript
// SIMPLIFY: Remove unnecessary async wrapper
// Before
async function getUser(id: string): Promise<User> {
  return await userService.findById(id);
}
// After
function getUser(id: string): Promise<User> {
  return userService.findById(id);
}

// SIMPLIFY: Optional chaining over null guards
// Before
const name = user !== null && user !== undefined ? user.profile.name : undefined;
// After
const name = user?.profile?.name;
```

```kotlin
// SIMPLIFY: let over null check
// Before
if (user != null) { sendWelcomeEmail(user) }
// After
user?.let { sendWelcomeEmail(it) }

// SIMPLIFY: when over if-else chain
// Before
if (role == "admin") return Admin()
else if (role == "viewer") return Viewer()
else return Guest()
// After
return when (role) {
  "admin" -> Admin()
  "viewer" -> Viewer()
  else -> Guest()
}
```

## Common Rationalizations

| Rationalization | Reality |
|---|---|
| "It works, don't touch it" | Working code that's hard to read accumulates maintenance cost silently. |
| "The next developer will figure it out" | They won't, or they'll spend an hour doing so. That's waste. |
| "Simplification might break something" | That's what tests are for. Run them after each change. |
| "I'll clean this up later" | You won't. Simplify now while the context is fresh. |
| "This is the pattern we've always used here" | Patterns can be wrong. If the code is unclear, it's worth questioning. |

## Verification Checklist

- [ ] Behavior is provably identical (all tests pass without modification)
- [ ] Simplification is scoped to the changed code (no drive-by cleanup)
- [ ] Each simplification committed separately from feature work
- [ ] Project conventions followed (not external preferences imposed)
- [ ] Chesterton's Fence respected (understood why before changing)
- [ ] No new abstractions added for single-use operations
