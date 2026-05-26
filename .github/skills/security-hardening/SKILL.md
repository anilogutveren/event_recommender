---
name: security-hardening
description: >
  OWASP Top 10 security review and hardening for Vue + Kotlin applications. Use when
  implementing auth, handling user input, designing APIs, or before any release.
  Triggers: "security review", "OWASP", "authentication", "authorization", "security check".
---

# Skill: Security Hardening

## The Security Mindset

All external input is hostile until validated. Clients lie. Users make mistakes.
Attackers probe boundaries. Every boundary must validate independently.

## Critical Finding Patterns

Immediately flag as **Critical** (block merge/release):

| Pattern | Vulnerability | Where to Check |
|---|---|---|
| String interpolation in SQL | SQL Injection (A03) | Repository / query methods |
| `v-html` with user data | XSS (A03) | Vue templates |
| Hardcoded secret / password | Credential Exposure (A02) | Config files, source code |
| No authorization check on endpoint | Broken Access Control (A01) | Every controller method |
| `eval()` or `new Function()` with user data | Code Injection (A03) | Any JS/TS file |
| Plaintext or MD5/SHA-1 password storage | Cryptographic Failure (A02) | Auth service, DB schema |
| No rate limiting on auth endpoints | Auth Failure (A07) | Login, password reset |

## OWASP Top 10 — Vue + Kotlin Mitigations

### A01: Broken Access Control

```kotlin
// Check authorization on EVERY endpoint — never trust the frontend
@GetMapping("/api/v1/users/{id}/private-data")
fun getPrivateData(
    @PathVariable id: UUID,
    @AuthenticationPrincipal principal: UserPrincipal
): ResponseEntity<PrivateDataResponse> {
    // Verify the authenticated user is allowed to access this resource
    if (principal.userId != id && !principal.hasRole(UserRole.ADMIN)) {
        throw ForbiddenException("Access denied")
    }
    return ResponseEntity.ok(userService.getPrivateData(id))
}
```

### A02: Cryptographic Failures

```kotlin
// Password hashing — bcrypt only
@Bean
fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder(12)

// Never log or return password hashes
// Never store passwords in plaintext, MD5, or SHA-1
// Use TLS for all external communication
```

### A03: Injection

```kotlin
// ✅ Parameterized queries only
val users = Users.select { Users.email eq request.email.lowercase() }

// ❌ NEVER string interpolation in SQL
val query = "SELECT * FROM users WHERE email = '${request.email}'" // NEVER
```

```ts
// Vue: Output encoding is handled by Vue's template system automatically
// But for v-html (avoid where possible):
import DOMPurify from 'dompurify'
const safeHtml = computed(() => DOMPurify.sanitize(props.content))
```

### A05: Security Misconfiguration

```kotlin
// Required security headers (Spring Security)
http.headers {
    frameOptions { deny() }
    contentTypeOptions { }
    httpStrictTransportSecurity {
        includeSubDomains = true
        maxAgeInSeconds = 31536000
    }
}

// Explicit CORS — never wildcard in production
http.cors {
    configurationSource = CorsConfigurationSource {
        CorsConfiguration().apply {
            allowedOrigins = listOf("https://yourdomain.com")
            allowedMethods = listOf("GET", "POST", "PATCH", "DELETE")
            allowCredentials = true
        }
    }
}
```

### A07: Authentication Failures

```kotlin
// JWT configuration
const val JWT_ACCESS_TOKEN_EXPIRY = 15L // minutes
const val JWT_REFRESH_TOKEN_EXPIRY = 7L // days

// Rate limiting on auth endpoints
@RateLimiter(name = "auth", fallbackMethod = "rateLimitFallback")
@PostMapping("/api/v1/auth/login")
fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<TokenResponse>

// Rotate refresh tokens on use (invalidate old, issue new)
suspend fun refreshTokens(refreshToken: String): TokenPair {
    val stored = tokenRepository.findByToken(refreshToken)
        ?: throw UnauthorizedException("Invalid refresh token")
    tokenRepository.delete(stored) // invalidate old token
    return issueNewTokenPair(stored.userId)
}
```

```ts
// Vue: Never store tokens in localStorage
// ✅ Correct: httpOnly cookie set by server
// ❌ Wrong:
localStorage.setItem('accessToken', token) // NEVER
```

## Input Validation — Two-Layer Rule

```
Layer 1 (Frontend / Vue): UX validation
  → Provides immediate feedback
  → NOT a security boundary

Layer 2 (Backend / Kotlin): Security validation
  → The real boundary
  → Always validates, regardless of frontend
```

## Secrets Management

```bash
# .env files NEVER committed to git
.env
.env.local
.env.*.local

# Use environment variables or secrets manager
# Spring Boot
spring.datasource.password=${DB_PASSWORD}

# Kotlin — read from environment
val dbPassword = System.getenv("DB_PASSWORD")
    ?: error("DB_PASSWORD environment variable is required")
```

## Sensitive Data Logging Rules

```kotlin
// ❌ Never log these
logger.info("User login: email=${request.email}, password=${request.password}")

// ✅ Log non-sensitive identifiers only
logger.info("User login attempt: userId=${user.id}, ip=${request.remoteAddr}")
```

## Pre-Release Security Checklist

- [ ] `npm audit` — no high/critical vulnerabilities
- [ ] `./gradlew dependencyCheckAnalyze` — check OWASP NVD database
- [ ] No secrets in codebase (`gitleaks` scan)
- [ ] All auth endpoints rate-limited
- [ ] Security headers verified with [securityheaders.com](https://securityheaders.com)
- [ ] HTTPS only (no HTTP in production)
- [ ] Input validation on all API boundaries
- [ ] No stack traces in error responses
- [ ] JWT expiry and rotation working correctly
- [ ] CORS configured explicitly (no `*` in production)
