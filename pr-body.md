## Summary

Implements a fully working event_recommender service following the Hexagonal Architecture (Ports & Adapters) defined in ADR-0001. The domain layer has zero framework dependencies; all cross-boundary communication goes through typed port interfaces.

## Changes

### New Source Files (25)
- **Domain**: `Event`, `EventId`, `Location`, `Category`, `EventTag`, `EventDomainService`, domain events
- **Application ports (inbound)**: `CreateEventUseCase`, `GetEventUseCase`, `RecommendEventsUseCase`
- **Application ports (outbound)**: `EventRepository`, `EventMetricsPort`, `DomainEventPublisher`
- **Application service**: `EventApplicationService` (orchestrates domain, no business logic)
- **REST adapter**: `EventController`, `CreateEventRequest`, `RecommendEventsRequest`, `EventResponse`, `GlobalExceptionHandler`
- **Persistence adapter**: `ElasticsearchEventRepositoryAdapter`, `EventDocument`, `EventElasticsearchRepository`
- **Monitoring adapter**: `MicrometerEventMetrics` (es.query.duration, es.index.operations), `SpringDomainEventPublisher`
- **Config**: `ApplicationServiceConfig`

### Modified Files
- `build.gradle.kts` — Spring Boot 4 module split: `spring-boot-starter-webmvc`, `spring-boot-starter-webmvc-test`
- `GlobalExceptionHandler` — added `HttpMessageNotReadableException` handler (500→400 bugfix, OWASP A05)
- `EventRequest.kt` — `@field:JsonSetter(nulls=AS_EMPTY)` on optional `tags` field

### New Test Files (6 suites, 61 tests)
- `EventTest` (11), `LocationTest` (10), `EventDomainServiceTest` (12), `EventApplicationServiceTest` (11), `EventControllerTest` (11), `EventDocumentTest` (6)

## Acceptance Criteria Coverage

| AC | Description | Status |
|----|-------------|--------|
| AC-1 | Hexagonal package structure | ✅ |
| AC-2 | Domain zero framework deps | ✅ |
| AC-3 | Ports are interfaces | ✅ |
| AC-4 | REST calls only Inbound Ports | ✅ |
| AC-5 | Persistence maps domain ↔ ES doc | ✅ |
| AC-6 | Custom Micrometer metrics | ✅ |
| AC-7 | Unit tests domain + app service | ✅ |
| AC-8 | Integration test for REST adapter | ✅ |
| AC-9 | `./gradlew build` passes green | ✅ |
| AC-10 | Health/metrics endpoints remain | ✅ |

## Test Results
- ✅ Passed: 61 | ❌ Failed: 0 | Skipped: 0
- Full report: `.stage/GH-3/testResults.md`
- **Bug found & fixed during testing**: `POST /api/v1/events` malformed body was returning 500 → fixed to 400 (OWASP A05)

## SDLC Score
Overall: **9.1 / 10** — see `.stage/GH-3/score.md`

| Phase | Score |
|-------|-------|
| PLAN | 8.5 / 10 |
| ARCHITECTURE | 9.0 / 10 |
| CODE (Implement) | 9.6 / 10 |
| TEST | 9.0 / 10 |
| RELEASE | 9.2 / 10 |

## Linked Issue
Closes #3

## Checklist
- [x] Tests pass (61/61, BUILD SUCCESSFUL)
- [x] Security checklist passed — OWASP A01–A10 reviewed; A01/A07 auth deferred (out of GH-3 scope)
- [x] Architecture decisions followed (ADR-0001 hexagonal architecture)
- [x] No hardcoded secrets or credentials
- [x] No stack traces in error responses (tested via TEST-R08, OWASP A05)
- [x] All stage artifacts committed alongside code changes

Generated with [Devin](https://cli.devin.ai/docs)
