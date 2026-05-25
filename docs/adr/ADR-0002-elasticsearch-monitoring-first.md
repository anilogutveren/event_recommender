# ADR-0002 — Elasticsearch 8.x as Primary Store with Monitoring-First Approach

**Date:** 2026-05-25
**Status:** Accepted
**Ticket:** FEAT-kotlin-es-monitoring

---

## Context

The event recommender needs:
- Full-text search over event names, descriptions, venues, categories
- Geo-distance filtering (events near a user's location)
- Future: vector similarity search for LLM-powered recommendations (ES 8 native KNN)
- Observability from day one — all ES operations must emit metrics before any domain logic is written

The team decision is to set up monitoring infrastructure *before* building persistence models, so every ES operation is instrumented from its first use.

---

## Decision

Use **Elasticsearch 8.x** as the primary data store, configured via `spring-data-elasticsearch`.
Monitoring is established first using:
- **Spring Boot Actuator** — `/actuator/health` with ES cluster health sub-indicator
- **Micrometer** — custom metrics for ES query latency, indexing throughput, and document count
- **OpenTelemetry** — distributed traces spanning HTTP → ES round-trips
- **Prometheus scrape endpoint** — `/actuator/prometheus`

No SQL/relational database is introduced until a clear need exists.

---

## Alternatives Considered

| Option | Pros | Cons |
|--------|------|------|
| **Elasticsearch 8.x** ✅ | Full-text + geo + vector search, native KNN, rich Spring integration | Heavier than a relational DB; requires running ES locally/in Docker |
| PostgreSQL + pgvector | Familiar SQL, good for structured data | Weaker full-text search; vector extension less mature than ES KNN |
| MongoDB | Flexible schema, decent text search | No native vector search without Atlas; weaker geo support |
| Meilisearch | Simple, fast | No vector search; no Spring integration |

---

## Monitoring Stack Detail

| Signal | Tool | Endpoint / Source |
|--------|------|-------------------|
| Health | Spring Actuator + ES health indicator | `GET /actuator/health` |
| Metrics | Micrometer + Prometheus registry | `GET /actuator/prometheus` |
| ES query latency | Custom `Timer` in ES repository wrappers | Metric: `es.query.duration` |
| ES index operations | Custom `Counter` in indexing service | Metric: `es.index.operations` |
| ES document count | Scheduled gauge | Metric: `es.documents.total` |
| Traces | OpenTelemetry Java agent (OTLP export) | Configured via `OTEL_EXPORTER_OTLP_ENDPOINT` |

---

## Consequences

- **Positive:** All ES operations are observable before any domain model exists — easier to catch performance regressions early
- **Positive:** ES 8 KNN vectors are available for future LLM-powered semantic recommendations without migration
- **Positive:** Prometheus metrics can feed Grafana dashboards immediately
- **Negative / Trade-off:** Local development requires a running ES instance (Docker Compose provided)
- **Negative / Trade-off:** ES is heavier than a simple DB for early-stage development; justified by the search and monitoring requirements

## References

- Architecture: `.stage/docs/architecture.md`
- ADR-0001: Kotlin + Spring Boot choice
