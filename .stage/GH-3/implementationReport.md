# GH-3 — Implementation Report

## SDLC Progress — GH-3
- [x] PLAN Phase
- [x] CODE Phase (Architecture)
- [x] CODE Phase (Implement)
- [ ] TEST Phase
- [ ] RELEASE Phase

---

## What Was Built

### Summary
Implemented the full Hexagonal Architecture (Ports & Adapters) for the `event_recommender` service, following ADR-0001 and the acceptance criteria in `plan.md`.

---

## Architecture Layers Implemented

### Domain Layer (`domain/`)
| File | Purpose |
|------|---------|
| `model/Event.kt` | Immutable `data class` — core domain entity with business rules in `init` |
| `model/EventId.kt` | Value class wrapping UUID |
| `model/Location.kt` | Value class with lat/lon/city/country, Haversine distance |
| `model/Category.kt` | Enum of event categories |
| `model/EventTag.kt` | Value class for tags |
| `event/EventCreatedEvent.kt` | Domain event published on creation |
| `event/EventUpdatedEvent.kt` | Domain event published on update |
| `service/EventDomainService.kt` | Pure business logic — filter + rank events |

**Zero framework dependencies** — no Spring, no Jackson annotations in domain.

### Application Layer (`application/`)
| File | Purpose |
|------|---------|
| `port/inbound/CreateEventUseCase.kt` | Inbound port — create event |
| `port/inbound/GetEventUseCase.kt` | Inbound port — find/list events |
| `port/inbound/RecommendEventsUseCase.kt` | Inbound port — recommendation |
| `port/outbound/EventRepository.kt` | Outbound port — persistence |
| `port/outbound/EventMetricsPort.kt` | Outbound port — metrics |
| `port/outbound/DomainEventPublisher.kt` | Outbound port — domain events |
| `service/EventApplicationService.kt` | Orchestrator — implements all 3 inbound ports |

### Adapter Layer (`adapter/`)
| File | Purpose |
|------|---------|
| `inbound/rest/EventController.kt` | REST driving adapter — calls only Inbound Ports |
| `inbound/rest/EventRequest.kt` | DTOs: `CreateEventRequest`, `RecommendEventsRequest` |
| `inbound/rest/EventResponse.kt` | DTO: `EventResponse` with `fromDomain()` factory |
| `inbound/rest/GlobalExceptionHandler.kt` | `@RestControllerAdvice` — maps domain exceptions |
| `outbound/persistence/EventDocument.kt` | ES persistence model — no domain annotations |
| `outbound/persistence/EventElasticsearchRepository.kt` | Spring Data ES repository interface |
| `outbound/persistence/ElasticsearchEventRepositoryAdapter.kt` | Implements `EventRepository` port |
| `outbound/monitoring/MicrometerEventMetrics.kt` | Implements `EventMetricsPort` (Micrometer) |
| `outbound/monitoring/SpringDomainEventPublisher.kt` | Implements `DomainEventPublisher` |

### Config Layer (`config/`)
| File | Purpose |
|------|---------|
| `config/ApplicationServiceConfig.kt` | Spring wiring — creates `EventApplicationService` bean |

---

## Key Design Decisions

1. **No Spring annotations in domain** — `Event`, `Location`, etc. are pure Kotlin with zero imports from Spring/Jackson
2. **Coroutine-based ports** — all `EventRepository` and use-case methods are `suspend`, bridged with `runBlocking` in REST adapter
3. **Adapter Pattern** — `ElasticsearchEventRepositoryAdapter` uses `@Profile("!test")` to exclude from test context
4. **Jackson null-safe tags** — `@field:JsonSetter(nulls = Nulls.AS_EMPTY)` on `tags` field in `CreateEventRequest`
5. **Custom Micrometer metrics** — `es.query.duration` (Timer), `es.index.operations` (Counter), `es.documents.total` (Gauge)

---

## Spring Boot 4 Migration Notes (encountered during implementation)

| Issue | Resolution |
|-------|-----------|
| `@WebMvcTest` moved to `org.springframework.boot.webmvc.test.autoconfigure` | Added `spring-boot-starter-webmvc-test` dependency; updated imports |
| `@AutoConfigureMockMvc` moved to same package | Updated import |
| `spring-boot-starter-web` deprecated | Changed to `spring-boot-starter-webmvc` |
| ES auto-config class paths changed in SB4 | Updated `application-test.yml` exclusions to `org.springframework.boot.data.elasticsearch.autoconfigure.*` |
| `ElasticsearchEventRepositoryAdapter` loaded in test context | Added `@Profile("!test")` |

---

## Files Changed / Created

### New Source Files (25)
- `src/main/kotlin/com/eventrecommender/domain/**` (8 files)
- `src/main/kotlin/com/eventrecommender/application/**` (7 files)
- `src/main/kotlin/com/eventrecommender/adapter/**` (9 files)
- `src/main/kotlin/com/eventrecommender/config/ApplicationServiceConfig.kt`

### New Test Files (4)
- `src/test/kotlin/.../domain/model/EventTest.kt`
- `src/test/kotlin/.../domain/model/LocationTest.kt`
- `src/test/kotlin/.../domain/service/EventDomainServiceTest.kt`
- `src/test/kotlin/.../application/service/EventApplicationServiceTest.kt`
- `src/test/kotlin/.../adapter/inbound/rest/EventControllerTest.kt`
- `src/test/resources/application-test.yml`

### Modified Files
- `build.gradle.kts` — added `spring-boot-starter-webmvc-test` test dep; changed to `spring-boot-starter-webmvc`
- `src/main/resources/application.yml` — kept existing config
- `docs/adr/` — reorganised ADR structure

---

## Build Result
```
BUILD SUCCESSFUL
28 tests completed, 0 failed
```
