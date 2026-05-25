# FEAT-kotlin-es-monitoring — Story Brief

## Ticket Context
- **Ticket ID**: FEAT-kotlin-es-monitoring
- **Title**: Project scaffold + Elasticsearch monitoring foundation
- **Type**: Feature (Greenfield setup)
- **Priority**: High — all subsequent features depend on this

## Problem Statement
The `event_recommender` project has no application code yet. Before building any recommendation logic or persistence models, we need a working Kotlin/Spring Boot project with Elasticsearch connectivity and full observability instrumentation. This ensures every future ES operation is monitored from the moment it is introduced.

## User Story
As a **developer on the event_recommender team**, I want a running Kotlin Spring Boot app with Elasticsearch health checks and Micrometer metrics, so that every ES operation emits observable signals from day one.

## Acceptance Criteria
- [ ] Given a running Spring Boot app, when `GET /actuator/health` is called, then it returns `200 OK` with ES cluster health status
- [ ] Given a running Spring Boot app, when `GET /actuator/prometheus` is called, then it returns Prometheus-format metrics including ES-related counters
- [ ] Given an ES connection is made, then query latency is captured as a `es.query.duration` Micrometer `Timer`
- [ ] Given the app starts, then a scheduled gauge reports `es.documents.total` per index
- [ ] Given ES is unreachable, when `GET /actuator/health` is called, then it returns `503` with `elasticsearch: DOWN`
- [ ] `./gradlew build` passes with zero test failures
- [ ] `docker-compose up` starts both the app and a local ES 8.x instance

## Scope
### In scope
- Kotlin Spring Boot 3 project scaffold (`build.gradle.kts`, `settings.gradle.kts`, app entry point)
- Elasticsearch 8.x client configuration (`ElasticsearchConfig.kt`)
- Spring Actuator with custom ES health indicator (`ElasticsearchHealthIndicator.kt`)
- Micrometer metrics: query duration timer, index operation counter, document count gauge
- OpenTelemetry configuration (OTLP export via env var)
- `docker-compose.yml` with ES 8.x + the Spring Boot app
- `application.yml` with configurable ES host/port via env vars

### Out of scope
- Domain models (Event, User, Recommendation) — next ticket
- Elasticsearch index schema / mappings — next ticket
- Authentication / JWT — future ticket
- Any recommendation logic — future ticket

## Technical Notes
- Data sources: Elasticsearch 8.x (local Docker for dev)
- Dependencies: `spring-boot-starter-web`, `spring-boot-starter-actuator`, `spring-boot-starter-data-elasticsearch`, `micrometer-registry-prometheus`, `io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter`
- Performance targets: health check response <50ms
- Security constraints: `/actuator/health` and `/actuator/prometheus` are public; all other actuator endpoints disabled

## Open Questions
- *(none — all decisions resolved in ADR-0001 and ADR-0002)*
