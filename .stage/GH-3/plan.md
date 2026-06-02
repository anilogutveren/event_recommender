# GH-3 — Plan: Extend event_recommender with Hexagonal Architecture

## SDLC Progress — GH-3
- [x] PLAN Phase
- [x] CODE Phase (Architecture)
- [x] CODE Phase (Implement)
- [x] TEST Phase — Completed ✅
- [ ] RELEASE Phase

---

## Requirements & Acceptance Criteria

### Goal
Implement a fully working event_recommender service following the Hexagonal Architecture (Ports & Adapters) defined in ADR-0001, using patterns from `/docs/adr/architecture/patterns/`.

### Domain Scope
- **Event** entity: id, title, description, category, location (geo), startTime, endTime, venue, tags
- **Recommendation** use case: find events matching user preferences (category, location radius, date range)
- **CRUD** use cases: create, find-by-id, list/search events

### Architecture Layers (per ADR-0001)
```
com.eventrecommender/
├── domain/
│   ├── model/           Event, EventId, Location, Category, EventTag
│   ├── service/         EventDomainService
│   └── event/           EventCreatedEvent, EventUpdatedEvent
├── application/
│   ├── port/
│   │   ├── inbound/     CreateEventUseCase, GetEventUseCase, RecommendEventsUseCase
│   │   └── outbound/    EventRepository, EventMetricsPort
│   └── service/         EventApplicationService
└── adapter/
    ├── inbound/
    │   └── rest/         EventController, EventRequest/Response DTOs
    └── outbound/
        ├── persistence/  ElasticsearchEventRepository, EventDocument
        └── monitoring/   MicrometerEventMetrics
```

### Acceptance Criteria
1. All packages follow the hexagonal structure exactly as in ADR-0001
2. Domain has zero framework dependencies (no Spring, no ES annotations)
3. All ports are interfaces; adapters implement them
4. REST controller calls only Inbound Port interfaces
5. Persistence adapter maps domain ↔ ES document (no domain models in adapters)
6. Custom Micrometer metrics: `es.query.duration`, `es.index.operations`
7. Unit tests for domain + application service (no Spring context)
8. Integration test for REST adapter (MockMvc / WebTestClient)
9. `./gradlew build` passes green
10. Existing health/metrics endpoints (`/actuator/health`, `/actuator/prometheus`) remain functional

### Patterns Applied
- **Ports & Adapters**: inbound/outbound ports as interfaces
- **Domain Model**: immutable `data class` / `value class`, no framework annotations
- **Application Services**: orchestrate domain, implement inbound ports, use outbound ports
- **Command/Query objects**: `CreateEventCommand`, `FindEventQuery`, `RecommendEventsQuery`
- **Repository Pattern**: `EventRepository` outbound port
- **Strategy Pattern**: `RecommendationStrategy` for recommendation logic variations
- **Adapter Pattern**: `ElasticsearchEventRepository` adapts ES to domain repository port
