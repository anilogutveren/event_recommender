# GH-3 — Test Design

**Date:** 2026-06-02
**Technique selection:** per `test-patterns` SKILL.md

---

## AC-to-Technique Mapping

| AC | Description | Technique Selected | Rationale |
|----|-------------|-------------------|-----------|
| AC-1 | Hexagonal package structure | Structural (compile-time) | Package layout is verified by successful compilation |
| AC-2 | Domain zero framework deps | Negative Testing | Assert no Spring/Jackson imports exist in domain classes |
| AC-3 | Ports are interfaces | Structural (compile-time) | Enforced by Kotlin type system |
| AC-4 | REST calls only Inbound Ports | BDD (Given/When/Then) | MockMvc tests stub ports and verify HTTP contract |
| AC-5 | Persistence maps domain ↔ ES doc | Equivalence Partitioning | Partition: all fields present, optional fields missing, tags empty |
| AC-6 | Custom Micrometer metrics | BDD | Verify meter names are registered and increment correctly |
| AC-7 | Unit tests domain + app service | BDD + Boundary Value | GWT structure; BVA on pagination, distance thresholds |
| AC-8 | Integration test for REST adapter | BDD + Negative Testing | Happy path + 400/404/500 error codes |
| AC-9 | `./gradlew build` passes | Characterization | Build must be green; verified by CI/Gradle |
| AC-10 | Actuator endpoints remain | BDD | GET /actuator/health returns 200 in test context |

---

## Test Cases

### Domain Layer — `Event`

| ID | AC | Technique | Precondition | Input | Expected | Priority |
|----|-----|-----------|-------------|-------|----------|---------|
| TEST-D01 | AC-2 | EP | none | `Event.create(title=" ")` | `IllegalArgumentException` | High |
| TEST-D02 | AC-2 | EP | none | `Event.create(description=" ")` | `IllegalArgumentException` | High |
| TEST-D03 | AC-2 | EP | none | `Event.create(venue=" ")` | `IllegalArgumentException` | High |
| TEST-D04 | AC-2 | BVA | none | `endTime == startTime` | `IllegalArgumentException` | High |
| TEST-D05 | AC-2 | EP | none | Valid `Event.create(...)` | Event with generated UUID id | High |
| TEST-D06 | AC-2 | EP | event created | `event.addTag(tag)` | `event.hasTag(tag) == true` | Medium |
| TEST-D07 | AC-2 | EP | event with tag | `event.removeTag(tag)` | `event.hasTag(tag) == false` | Medium |
| TEST-D08 | AC-2 | EP | event | `event.update(title="New")` | new event with same id, new title | Medium |

### Domain Layer — `Location`

| ID | AC | Technique | Precondition | Input | Expected | Priority |
|----|-----|-----------|-------------|-------|----------|---------|
| TEST-L01 | AC-2 | BVA | none | `latitude=91.0` | `IllegalArgumentException` | High |
| TEST-L02 | AC-2 | BVA | none | `latitude=-91.0` | `IllegalArgumentException` | High |
| TEST-L03 | AC-2 | BVA | none | `longitude=181.0` | `IllegalArgumentException` | High |
| TEST-L04 | AC-2 | BVA | none | `longitude=-181.0` | `IllegalArgumentException` | High |
| TEST-L05 | AC-2 | BVA | none | `latitude=90.0` (boundary) | Valid location | Medium |
| TEST-L06 | AC-2 | BVA | none | `latitude=-90.0` (boundary) | Valid location | Medium |
| TEST-L07 | AC-2 | EP | two locations | `berlin.distanceKmTo(berlin)` | `~0.0 km` | Medium |
| TEST-L08 | AC-2 | EP | two cities | `berlin.distanceKmTo(munich)` | `490–520 km` | Medium |
| TEST-L09 | AC-2 | EP | location + radius | `loc.isWithinRadius(same, 100)` | `true` | Medium |

### Domain Service — `EventDomainService`

| ID | AC | Technique | Precondition | Input | Expected | Priority |
|----|-----|-----------|-------------|-------|----------|---------|
| TEST-S01 | AC-7 | EP | no events | `filterByPreferences(emptyList, MUSIC, null, null)` | empty list | **High** |
| TEST-S02 | AC-7 | EP | events | `filterByPreferences(events, emptySet, null, null)` | all events | High |
| TEST-S03 | AC-7 | EP | mixed categories | `filterByPreferences(events, {MUSIC}, null, null)` | only MUSIC events | High |
| TEST-S04 | AC-7 | BVA | events near/far | `filterByPreferences(events, {}, munich, 600km)` | only within 600 km | High |
| TEST-S05 | AC-7 | EP | all past events | `filterByPreferences(pastEvents, {}, null, null)` | all past events returned (no date filter here) | Medium |
| TEST-S06 | AC-7 | EP | no events | `rank(emptyList, null)` | empty list | **High** |
| TEST-S07 | AC-7 | EP | upcoming + past | `rank(events, null)` | upcoming first | High |
| TEST-S08 | AC-7 | EP | events + location | `rank(events, munich)` | closest first | High |
| TEST-S09 | AC-7 | Decision Table | category+location | MUSIC + within radius → included | included | High |
| TEST-S10 | AC-7 | Decision Table | category+location | MUSIC + outside radius → excluded | excluded | High |
| TEST-S11 | AC-7 | Decision Table | category+location | wrong cat + within radius → excluded | excluded | High |

### Application Service — `EventApplicationService`

| ID | AC | Technique | Precondition | Input | Expected | Priority |
|----|-----|-----------|-------------|-------|----------|---------|
| TEST-A01 | AC-7 | BDD | repo mock | `CreateEventCommand` | saved event returned, domain event published | High |
| TEST-A02 | AC-7 | BDD | event in repo | `FindEventQuery(id)` | event returned | High |
| TEST-A03 | AC-7 | Negative | empty repo | `FindEventQuery(unknown id)` | `null` returned | High |
| TEST-A04 | AC-7 | BDD | events in repo | `ListEventsQuery(page=0, size=20)` | list from repo | High |
| TEST-A05 | AC-7 | BDD | events in repo | `RecommendEventsQuery` | filtered + ranked list | High |
| TEST-A06 | AC-7 | BVA | repo mock | `ListEventsQuery(page=0, size=1)` | size=1 list | Medium |
| TEST-A07 | AC-6 | EP | metrics mock | create event | `recordQueryDuration` and `incrementIndexOperation` called | High |

### REST Adapter — `EventController`

| ID | AC | Technique | Precondition | Input | Expected | Priority |
|----|-----|-----------|-------------|-------|----------|---------|
| TEST-R01 | AC-4,8 | BDD | event exists | `GET /api/v1/events/{id}` | `200` + JSON body | High |
| TEST-R02 | AC-4,8 | Negative | no event | `GET /api/v1/events/unknown` | `404` | High |
| TEST-R03 | AC-4,8 | BDD | use case mock | `POST /api/v1/events` valid body | `201` + id in body | High |
| TEST-R04 | AC-4,8 | Negative | validation | `POST /api/v1/events` missing title | `400` | **High** |
| TEST-R05 | AC-4,8 | Negative | validation | `POST /api/v1/events` invalid category | `400` | High |
| TEST-R06 | AC-4,8 | BDD | events exist | `GET /api/v1/events` | `200` + array | High |
| TEST-R07 | AC-4,8 | BVA | empty repo | `GET /api/v1/events` → empty list | `200` + `[]` | **High** |
| TEST-R08 | AC-8 | OWASP | none | error response body | no stack trace leaked | **High** |
| TEST-R09 | AC-4,8 | BDD | use case mock | `POST /api/v1/events/recommendations` | `200` + array | High |
| TEST-R10 | AC-10 | BDD | spring context | `GET /actuator/health` | `200` | Medium |

---

## Mandatory Edge Cases (from test-patterns skill)

| Edge Case | Test ID | Status |
|-----------|---------|--------|
| Empty event catalogue | TEST-S01, TEST-S06, TEST-R07 | To be added |
| All events in past | TEST-S05 | To be added |
| Cold start (no preferences) | TEST-S02 | ✅ Covered |
| Pagination: page 0 | TEST-A04 | ✅ Covered |
| Pagination: size=1 (boundary) | TEST-A06 | To be added |
| Stack trace not in response | TEST-R08 | To be added |
| Invalid input → 400 | TEST-R04, TEST-R05 | To be added |
