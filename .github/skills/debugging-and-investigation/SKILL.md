---
name: debugging-and-investigation
description: >
  Systematic 4-phase debugging workflow: reproduce, localize, root cause, fix and guard.
  Use when tests fail, bugs are reported, or runtime behavior is unexpected. Triggers:
  "debug", "why is this failing", "bug", "error", "not working", "investigate".
---

# Skill: Debugging & Investigation

## The Iron Law

**NO FIX WITHOUT ROOT CAUSE FIRST.**

Fixing symptoms causes whack-a-mole debugging. Every fix that doesn't address the root
cause creates two more bugs.

## 4-Phase Process

### Phase 1: Reproduce Deterministically

1. Read the full error message and stack trace.
2. Identify the exact steps that trigger the issue.
3. Write a minimal reproduction case.
4. Confirm you can trigger it reliably.

If you can't reproduce it: add logging → gather more data → return to step 1.

### Phase 2: Localize the Cause

```bash
# Check recent changes on affected files
git log --oneline -20 -- path/to/affected-file.kt

# Find when the bug was introduced
git bisect start
git bisect bad HEAD
git bisect good <known-good-commit>
```

Trace the data flow from symptom back to cause:
- What data enters the affected code?
- Where does it get transformed?
- Where does it deviate from expected behavior?

### Phase 3: Pattern Analysis

| Pattern | Symptoms | Root Cause Area |
|---|---|---|
| Race condition | Intermittent, timing-dependent | Concurrent access to shared state |
| Null propagation | `NullPointerException`, `TypeError` | Missing null guard |
| State corruption | Inconsistent data | Shared mutable state, missing transactions |
| Integration failure | Timeout, unexpected response | External API boundary |
| Config drift | Works locally, fails in staging | Env vars, feature flags |
| Cache staleness | Shows old data, clears on refresh | Redis, CDN, browser cache |

**3-Strike Rule:** After 3 failed hypotheses, stop and reconsider from Phase 1.

### Phase 4: Fix and Guard

1. Fix the **root cause**, not the symptom.
2. Minimal diff — fewest files and lines touched.
3. Write a regression test that:
   - **FAILS** without the fix
   - **PASSES** with the fix
4. Run full test suite — nothing regressed.

## Red Flags During Debugging

| Red Flag | Why It's a Problem |
|---|---|
| "Quick fix for now" | There is no "for now". You're creating tech debt. |
| Proposing a fix before tracing the data flow | You're guessing, not debugging. |
| Each fix reveals a new problem elsewhere | You're fixing at the wrong layer. |
| Catch exception → return empty result | You're hiding the real error. |

## Vue Frontend Debugging

```ts
// 1. Check Vue DevTools for component state
// 2. Add temporary logging to composable
watch(user, (newVal, oldVal) => {
  console.debug('[useUser] user changed:', { from: oldVal, to: newVal })
}, { immediate: true })

// 3. Check network tab for API response shape
// 4. Isolate: render component with hardcoded props to rule out data source
```

## Kotlin Backend Debugging

```kotlin
// 1. Read the full stack trace — the root cause is usually the LAST cause in the chain
// 2. Add structured logging at suspected boundaries
logger.debug("Processing request: userId={}, action={}", userId, action)

// 3. Check for N+1: enable SQL logging in test
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

// 4. Check coroutine context
coroutineContext[CoroutineName]  // which coroutine threw?
```

## Regression Test Pattern (Prove-It)

```kotlin
@Test
fun `getUser - does NOT return password hash in response`() = runTest {
    // This test exists because of bug BUG-42: password_hash was exposed in API response
    val user = createTestUser(passwordHash = "secret-hash")
    val response = userService.getUserResponse(user.id)
    assertThat(response).doesNotContainKey("passwordHash")
    assertThat(response).doesNotContainKey("password")
}
```

```ts
// Vue: regression test pattern
it('does not expose auth token in rendered HTML', () => {
  const wrapper = mount(UserMenu, { props: { token: 'secret-token' } })
  expect(wrapper.html()).not.toContain('secret-token')
})
```

## Verification Checklist

- [ ] Issue reproduced deterministically before attempting fix
- [ ] Root cause identified (not just symptom)
- [ ] Less than 3 hypotheses tried before rechecking Phase 1
- [ ] Fix addresses root cause (not symptom)
- [ ] Regression test written (fails without fix, passes with fix)
- [ ] Full test suite passes after fix
- [ ] Minimal diff (no unrelated changes)
