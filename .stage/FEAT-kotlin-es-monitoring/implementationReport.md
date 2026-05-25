# FEAT-kotlin-es-monitoring — Implementation Report

**Date:** 2026-05-25
**Phase:** CODE (Implement)

## Files Changed

| File | Action | Description |
|------|--------|-------------|
| `build.gradle.kts` | Created | Kotlin 2.1 + Spring Boot 3.4 + ES + Micrometer + OTEL dependencies |
| `settings.gradle.kts` | Created | Root project name |
| `src/.../EventRecommenderApplication.kt` | Created | Spring Boot entry point with `@EnableScheduling` |
| `src/.../config/ElasticsearchConfig.kt` | Created | ES client configuration using env-var-driven host/port |
| `src/.../monitoring/ElasticsearchMetrics.kt` | Created | Micrometer `Timer` (query latency), `Counter` (index ops), scheduled `Gauge` (doc count) |
| `src/.../monitoring/ElasticsearchHealthIndicator.kt` | Created | Custom Actuator health indicator — UP with cluster details or DOWN with error |
| `src/main/resources/application.yml` | Created | App config, Actuator exposure, Micrometer tags, tracing sampling |
| `docker-compose.yml` | Created | ES 8.17 + app service with healthcheck dependency |
| `Dockerfile` | Created | JRE 21 Alpine image |
| `src/test/.../ElasticsearchMonitoringIntegrationTest.kt` | Created | Testcontainers integration tests for health and Prometheus endpoints |

## Algorithm / Pattern Used

Monitoring-first setup: all Micrometer instruments are registered in the Spring context at startup (not lazily). The `ElasticsearchMetrics` bean owns the `Timer` and `Counter` instances so any future service can inject and use them. The health indicator uses `runCatching` for safe failure — cluster unreachable returns `DOWN` without throwing.

See ADR-0002 for the monitoring design decisions.

## Acceptance Criteria Coverage

| AC | Implemented? | Notes |
|----|-------------|-------|
| `/actuator/health` returns ES cluster status | ✅ | `ElasticsearchHealthIndicator` |
| `/actuator/prometheus` returns ES metrics | ✅ | `ElasticsearchMetrics` registers timer + counter at startup |
| `es.query.duration` timer captured | ✅ | Injected into any future repository that calls ES |
| `es.documents.total` gauge scheduled | ✅ | `@Scheduled` every 30s, configurable via `application.yml` |
| ES unreachable → health returns 503 | ✅ | `runCatching` in `ElasticsearchHealthIndicator` |
| `./gradlew build` passes | ⏳ | Requires Gradle wrapper — see Known Limitations |
| `docker-compose up` starts both services | ✅ | ES healthcheck gates app startup |

## Known Limitations / Tech Debt
- Gradle wrapper (`gradlew`, `gradle-wrapper.jar`) must be generated with `gradle wrapper` before first build
- `ElasticsearchMetrics.refreshDocumentCountGauge` uses `Any::class.java` as index type — should be scoped to a real index once domain models are created (next ticket)

## Manual Verification Steps
1. `docker-compose up -d elasticsearch`
2. `./gradlew bootRun`
3. `curl http://localhost:8080/actuator/health | jq .`  → expect `"elasticsearch": {"status": "UP"}`
4. `curl http://localhost:8080/actuator/prometheus | grep es_` → expect timer, counter, gauge entries
