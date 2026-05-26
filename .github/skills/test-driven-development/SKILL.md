---
name: test-driven-development
description: >
  Red-Green-Refactor TDD workflow for any feature or bug fix. Use BEFORE writing any
  production code. Triggers: "implement feature", "fix bug", "add test", "TDD".
---

# Skill: Test-Driven Development

## The Iron Law

```
NO PRODUCTION CODE WITHOUT A FAILING TEST FIRST
```

There are no exceptions. "Simple" code still breaks. A test takes 30 seconds to write.

## Red-Green-Refactor Cycle

```
RED
  Write one minimal failing test
  Run it — verify it FAILS for the right reason
         ↓ (wrong failure → fix the test)
GREEN
  Write the SIMPLEST code that makes the test pass
  No more, no less. Hard-code if necessary.
  Run test — verify it PASSES
         ↓ (still failing → fix code, never the test)
REFACTOR
  Clean up: remove duplication, clarify names
  Run all tests — all must still pass
         ↓
Next behavior → repeat from RED
```

## Bug Fix Variant: Prove-It Pattern

```
1. Write a test that REPRODUCES the bug
2. Run it — VERIFY it fails (the bug is real)
3. Fix the root cause (not the symptom)
4. Run test — verify it PASSES
5. Run full test suite — nothing regressed
```

**Never fix a bug without a reproducing test. The test proves you fixed the right thing.**

## Test Size & Granularity

| Quality | Good Test | Bad Test |
|---|---|---|
| Minimal | Tests ONE behavior | Tests "email AND password AND format" |
| Clear name | `returns 404 when user not found` | `test1`, `testGetUser` |
| Independent | Passes in any order | Depends on previous test state |
| Fast | < 100ms (unit) | Sleeps, waits, network calls |

Split the test if you use "and" to describe what it tests.

## What NOT to Do After Writing Code First

If code was written before the test:
- Delete it. No exceptions.
- "Keep as reference" is not allowed.
- "I'll adapt the tests to it" defeats the purpose.
- Sunk cost is not a reason.

## Rationalizations Table

| Excuse | Reality |
|---|---|
| "This is too simple to test" | Simple code breaks. Test takes 30 seconds. |
| "I'll write tests after" | Tests written after pass immediately — they prove nothing. |
| "I already manually tested it" | Manual testing is not systematic. It won't catch regressions. |
| "TDD slows me down" | TDD is faster than debugging. The slowdown is upfront, savings compound. |
| "I'll add tests before merge" | You'll be in a hurry. You'll skip them. |
| "The acceptance criteria is the test" | Code is not a test. It can't run automatically. |

## Vue + Vitest TDD Example

```
RED: Write failing test
```
```ts
it('shows error message when email is invalid', async () => {
  const wrapper = mount(RegisterForm)
  await wrapper.fill('[data-testid="email-input"]', 'not-an-email')
  await wrapper.find('[data-testid="submit-btn"]').trigger('click')
  expect(wrapper.find('[data-testid="email-error"]').text())
    .toBe('Please enter a valid email address')
})
// Run: FAIL — element not found (correct: feature not implemented)
```

```
GREEN: Minimal implementation
```
```vue
<span v-if="emailError" data-testid="email-error">{{ emailError }}</span>
```

```
REFACTOR: Extract validation to composable
```

## Kotlin + JUnit TDD Example

```
RED: Write failing test
```
```kotlin
@Test
fun `getUser - throws NotFoundException when user does not exist`() = runTest {
    coEvery { userRepository.findById(any()) } returns null
    assertThrows<NotFoundException> { userService.getUser(UUID.randomUUID()) }
}
// FAIL: NotFoundException not thrown (method returns null)
```

```
GREEN: Minimal implementation
```
```kotlin
suspend fun getUser(id: UUID): User =
    userRepository.findById(id) ?: throw NotFoundException("User $id not found")
// PASS
```

```
REFACTOR: Add logging, improve error message
```

## Framework Quick Reference

### Vue + Vitest (frontend)

```ts
// Unit: composable
import { useCounter } from './use-counter'
it('increments count', () => {
  const { count, increment } = useCounter()
  increment()
  expect(count.value).toBe(1)
})

// Component
import { mount } from '@vue/test-utils'
it('shows error when email invalid', async () => {
  const w = mount(RegisterForm)
  await w.find('[data-testid="email-input"]').setValue('bad')
  await w.find('[data-testid="submit-btn"]').trigger('click')
  expect(w.find('[data-testid="email-error"]').text()).toBeTruthy()
})
```

### Kotlin + JUnit 5 + MockK (backend)

```kotlin
// Unit: service
@Test
fun `getUser - throws NotFoundException when user absent`() = runTest {
  coEvery { userRepository.findById(any()) } returns null
  assertThrows<NotFoundException> { userService.getUser(UUID.randomUUID()) }
}

// Integration: API boundary (Testcontainers auto-starts)
@Test
fun `POST users - returns 201 with created user`() {
  val response = restTemplate.postForEntity("/api/v1/users", validRequest, UserResponse::class.java)
  assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
}
```

## Test Anti-Patterns

| Anti-Pattern | Problem | Fix |
|---|---|---|
| Testing implementation details | Test breaks on refactor even when behavior is unchanged | Test observable outputs, not internal method calls |
| Over-mocking | Mocks diverge from real behavior → false confidence | Mock at system boundary only (DB, HTTP, time) |
| Non-deterministic tests | Flaky tests erode trust in the suite | Eliminate random data; control time and I/O |
| Slow unit tests | > 100ms per test → developers stop running the suite | No network, no DB, no filesystem in unit tests |
| Tests that never fail | Tests written after the fact to hit coverage numbers | Run the test before the implementation — it MUST be RED |
| Testing multiple behaviors | `AND` in the test name is a split signal | One test, one assertion, one behavior |

## Verification Checklist

- [ ] Test written BEFORE production code
- [ ] Test ran and FAILED before implementation
- [ ] Implementation is minimal (no speculative features)
- [ ] Test runs and PASSES after implementation
- [ ] Full test suite still passes after refactor
- [ ] Each test covers exactly ONE behavior

## Coverage Targets

| Layer | Target | What to Cover |
|---|---|---|
| Unit tests | ~80% | Services, composables, domain logic |
| Integration tests | ~15% | API boundaries, DB operations |
| E2E tests | ~5% | Critical user journeys only |

**Design signal:** If a test is hard to write, the design is wrong. Stop, simplify the design, then test.
