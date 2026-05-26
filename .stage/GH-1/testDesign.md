# GH-1 — Test Design

**Date:** 2026-05-25
**Ticket:** Remove Elasticsearch integration

## Test Strategy
This is a deletion/cleanup ticket. Tests verify *absence* (no ES beans, no ES metrics,
no ES health component) and *basic liveness* (context loads, actuator responds).

## Test Cases

| ID | Description | Technique | Preconditions | Input | Expected Result | Priority |
|----|-------------|-----------|---------------|-------|-----------------|----------|
| TEST-001 | Spring context loads without Elasticsearch | Happy path | App built without ES dep | Start context | No `UnsatisfiedDependencyException` | Critical |
| TEST-002 | `/actuator/health` returns 200 OK | Happy path | Context loaded | GET /actuator/health | HTTP 200, `"status":"UP"` | Critical |
| TEST-003 | Health response has no `elasticsearch` component | Absence assertion | Context loaded | GET /actuator/health | Body does NOT contain `"elasticsearch"` | High |
| TEST-004 | `/actuator/prometheus` returns 200 OK | Happy path | Context loaded | GET /actuator/prometheus | HTTP 200 | High |
| TEST-005 | Prometheus output has no `es_*` metrics | Absence assertion | Context loaded | GET /actuator/prometheus | Body does NOT contain `es_query_duration`, `es_index_operations`, `es_documents_total` | High |

## Traceability
See `testTraceability.md`.
