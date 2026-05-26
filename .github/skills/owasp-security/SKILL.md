---
name: owasp-security
description: >
  OWASP Top 10 security checklist for Kotlin/Spring Boot REST APIs.
  Load this skill when reviewing or implementing code that handles
  HTTP requests, authentication, database access, or external API calls
  in the event_recommender project.
---

# OWASP Security Checklist — event_recommender

Apply this checklist during every implementation and test phase. A ticket cannot score 8+/10 in CODE or TEST phase with an open item below.

---

## A01 — Broken Access Control
- [ ] Every endpoint declares its required role/scope (e.g., `@PreAuthorize`)
- [ ] User A cannot fetch User B's recommendation history (ownership check on `/users/{id}/**`)
- [ ] No sensitive endpoints exposed without authentication (actuator endpoints disabled or secured in prod)
- [ ] IDOR: never expose raw internal DB IDs in URLs — use opaque tokens or UUIDs

## A02 — Cryptographic Failures
- [ ] No plaintext secrets, API keys, or credentials in source code or config files
- [ ] Secrets loaded from environment variables or Spring `@ConfigurationProperties` with encrypted backing store
- [ ] HTTPS enforced — no `http://` callback URLs or redirect targets
- [ ] Passwords hashed with bcrypt (`BCryptPasswordEncoder`), not MD5/SHA-1

## A03 — Injection
- [ ] All database queries use Spring Data repositories or parameterized `@Query` — no string concatenation
- [ ] Elasticsearch queries built via the official client query builder, not raw string interpolation
- [ ] External API response data is validated/sanitised before being stored or returned
- [ ] No `@RequestParam` / `@PathVariable` values passed directly into log messages without sanitisation

## A05 — Security Misconfiguration
- [ ] `spring.boot.admin` and `/actuator/*` endpoints are not publicly accessible in production profile
- [ ] CORS is explicitly configured — no wildcard `*` origin in production (`application-prod.yml`)
- [ ] Error responses return generic messages; stack traces are **never** included in API responses
- [ ] `spring.jpa.show-sql=true` is absent from production profile

## A06 — Vulnerable and Outdated Components
- [ ] No dependency with a known CVE in the current `build.gradle.kts`
- [ ] Verify with: `./gradlew dependencyCheckAnalyze` (OWASP Dependency-Check plugin)

## A07 — Identification and Authentication Failures
- [ ] JWT tokens have a short expiry (`exp` claim ≤ 1 hour for access tokens)
- [ ] Refresh token rotation is enforced — old refresh token invalidated on use
- [ ] Brute-force protection on auth endpoints (rate limiting or account lockout)
- [ ] Logout invalidates the token server-side (token blacklist or short-lived tokens only)

## A09 — Security Logging and Monitoring Failures
- [ ] Authentication successes and failures are logged with timestamp, user ID, and IP (never password)
- [ ] PII (email, full name, location) is **never** logged — use masked identifiers
- [ ] Structured logging used (`application.yml` configures logback/log4j2 in JSON format)
- [ ] Recommendation access for a user is logged for audit trail

## A10 — Server-Side Request Forgery (SSRF)
- [ ] External API base URLs (Ticketmaster, Eventbrite) come from config — never from user input
- [ ] URL allowlist enforced when fetching external event data — no arbitrary URL fetching

---

## Quick-fail Conditions (auto-fail CODE score below 6)
Any of the following **must** be fixed before PR creation:
1. Hardcoded secret or credential in committed code
2. Missing authentication on a data-access endpoint
3. Raw SQL/ES query string built from user input
4. Stack trace returned in HTTP error response
