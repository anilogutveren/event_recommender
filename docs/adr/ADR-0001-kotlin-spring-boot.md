# ADR-0001 — Kotlin + Spring Boot 3 + JVM 21 as Backend Stack

**Date:** 2026-05-25
**Status:** Accepted
**Ticket:** FEAT-kotlin-es-monitoring

---

## Context

The `event_recommender` project needs a backend stack that:
- Is familiar to the lead developer (Kotlin developer)
- Provides a mature, production-ready web framework
- Has first-class Elasticsearch, Micrometer, and OpenTelemetry integration
- Supports coroutines for async recommendation processing
- Compiles to JVM bytecode for portability

---

## Decision

Use **Kotlin 2.x** on **JVM 21** with **Spring Boot 3.x** and **Gradle (Kotlin DSL)**.

---

## Alternatives Considered

| Option | Pros | Cons |
|--------|------|------|
| **Kotlin + Spring Boot 4** ✅ | Familiar to dev, mature ecosystem, rich Spring Data ES support, Actuator out-of-box | JVM startup time (mitigated by GraalVM if needed) |
| Python + FastAPI | Fast iteration, rich ML libraries | Not idiomatic for Kotlin developer, weaker type safety |
| TypeScript + NestJS | Great DX, fast startup | Less mature ES integration, no coroutines |
| Kotlin + Ktor | Lightweight, idiomatic Kotlin | Fewer batteries included; would need to wire monitoring manually |

---

## Consequences

- **Positive:** Spring Data Elasticsearch, Spring Actuator, and Micrometer are all first-class Spring Boot citizens — minimal boilerplate for monitoring setup
- **Positive:** Kotlin data classes eliminate boilerplate for domain models
- **Positive:** Coroutines available via `spring-boot-starter-webflux` if async is needed later
- **Negative / Trade-off:** Spring Boot brings more startup overhead than Ktor; acceptable for a containerized service

## References

- Architecture: `.stage/docs/architecture.md`
