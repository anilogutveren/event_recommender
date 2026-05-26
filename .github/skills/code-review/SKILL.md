---
name: code-review
description: >
  Systematic 5-axis code review with severity labels. Use before merging any PR,
  after completing a feature, or when evaluating AI-generated code. Triggers:
  "review code", "review PR", "check quality", "code review".
---

# Skill: Code Review

## Overview

5-axis code review with consistent severity labeling. The approval standard:
**"Does this definitely improve the overall code health of the system?"**

## 5 Review Axes

1. **Correctness** — Does it do what it claims? Are there edge cases not handled?
2. **Readability & Simplicity** — Can the next developer understand this without explanation?
3. **Architecture** — Does it fit the existing patterns? Does it violate layer boundaries?
4. **Security** — Does it introduce vulnerabilities? (OWASP Top 10 check)
5. **Performance** — Are there obvious bottlenecks, N+1 queries, or memory leaks?

## Severity Labels

| Label | Meaning | Action required? |
|---|---|---|
| *(no prefix)* | Required change | Yes — must fix before merge |
| **Critical:** | Blocks merge (security vuln, data loss) | Yes — immediate fix |
| **Nit:** | Minor style/preference | No — author may ignore |
| **Optional:** | Suggestion | No — author decides |
| **Consider:** | Alternative worth thinking about | No — discussion |
| **FYI:** | Informational | No — awareness only |

## Comment Format

```
[SEVERITY] file.kt:42 — [specific issue]
[EXPLANATION of why it's a problem]
[CONCRETE FIX suggestion]
```

Example:
```
Critical: UserController.kt:67 — SQL injection via string interpolation
User-supplied `name` parameter is interpolated directly into the query.
Fix: Use parameterized query: `where("name = ?", request.name)`

Nit: UserService.kt:23 — variable name `d` is unclear
Consider renaming to `displayName` for readability.
```

## Change Size Limits

| Lines changed | Status |
|---|---|
| ~100 | ✅ Ideal |
| ~300 | ⚠️ Acceptable (single logical change) |
| ~500 | ⚠️ Review carefully — consider splitting |
| 1000+ | ❌ Too large — must split |

## Security Checklist (Required for Every PR)

- [ ] No hardcoded credentials or secrets
- [ ] All user input validated at boundaries
- [ ] No SQL string interpolation
- [ ] No `innerHTML` / `eval()` / `new Function()` with user data
- [ ] Auth checked on all protected endpoints
- [ ] No sensitive data in logs or error messages
- [ ] No PII exposed in API responses beyond what's needed

## Vue-Specific Review Points

- [ ] No `any` TypeScript types
- [ ] Props typed with `defineProps<{}>()` generics
- [ ] No prop mutation (emits used correctly)
- [ ] `v-for` has stable `:key`
- [ ] All interactive elements have `data-testid`
- [ ] Accessibility: semantic HTML, ARIA where needed
- [ ] Loading/error/empty states handled

## Kotlin-Specific Review Points

- [ ] No `!!` null assertions
- [ ] No `var` where `val` works
- [ ] All I/O is `suspend fun`
- [ ] `@Valid` on all request bodies
- [ ] No business logic in controllers
- [ ] Domain exceptions thrown (not raw HTTP codes from service)
- [ ] No exposed internal error details in responses
- [ ] Service tests cover happy path + failure cases
- [ ] Integration test covers API boundary

## Review Output

Save review output to: `.ai/reviews/YYYY-MM-DD-[pr-or-feature].md`

```markdown
## Code Review — [PR Title / Feature Name]

### ✅ What's Done Well
- [specific positive observation]

### Required Changes
- **Critical:** [file:line] — [issue + fix]
- [file:line] — [issue + fix]

### Suggestions
- **Optional:** [file:line] — [suggestion]
- **Nit:** [file:line] — [minor note]

### Verdict
APPROVE | REQUEST CHANGES

Reasoning: [one sentence]
```

## Goal-Backward Verification

Code review must verify that the **goal** was achieved, not just that the task was executed. A component can exist and be well-written but still miss the point.

Apply a 4-level check for every significant change:

| Level | Check | How to verify |
|---|---|---|
| **Exists** | The artifact is present | File exists, function is defined |
| **Substantive** | Content is real implementation, not a placeholder | No TODO stubs, no hardcoded dummy values, no empty functions |
| **Wired** | Connected to the rest of the system | Imported and called, route registered, component rendered |
| **Functional** | Actually works when invoked | Tests exercise it, or manual verification confirms behavior |

**Stub detection — scan for these patterns:**
```bash
# Placeholder indicators
grep -E "(TODO|FIXME|PLACEHOLDER|implement me)" <files>
grep -E "return null|return \{\}|return \[\]" <files>  # trivial no-ops
grep -E "coming soon|under construction|lorem ipsum" <files>
```

A review that confirms "the file exists" without checking all four levels is incomplete.

## Common Rationalizations

| Rationalization | Reality |
|---|---|
| "This is a nitpick, I'll skip it" | Nit-level feedback is explicitly part of this skill. Leave the nit, mark it as **Nit:**. |
| "The author knows what they're doing" | Trust but verify. Correctness and security don't care about seniority. |
| "I don't want to block the PR for this" | Use **Optional:** or **Consider:** labels. You can comment without blocking. |
| "The tests are there, so it's fine" | Tests that exist aren't always tests that prove the right thing. Review them. |
| "Security is someone else's job" | The security checklist is mandatory for every PR. There is no security specialist gate. |

## Verification Checklist

- [ ] All 5 axes reviewed (Correctness, Readability, Architecture, Security, Performance)
- [ ] Security checklist completed
- [ ] Vue-specific OR Kotlin-specific checklist completed as appropriate
- [ ] All Critical issues documented
- [ ] Verdict stated with reasoning
- [ ] At least one positive observation noted
