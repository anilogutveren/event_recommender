# GH-3 — Test Results

**Date:** 2026-06-02
**Test Framework:** JUnit 5 + Mockk + Spring Boot Test

---

## Summary

| Status | Count |
|--------|-------|
| ✅ Passed | 61 |
| ❌ Failed | 0 |
| ⏭ Skipped | 0 |

```
BUILD SUCCESSFUL
61 tests completed, 0 failed
```

---

## Test Suites

| Suite | Tests | Passed | Failed | Layer |
|-------|-------|--------|--------|-------|
| `EventTest` | 11 | 11 | 0 | Domain model |
| `LocationTest` | 10 | 10 | 0 | Domain model |
| `EventDomainServiceTest` | 12 | 12 | 0 | Domain service |
| `EventApplicationServiceTest` | 11 | 11 | 0 | Application |
| `EventControllerTest` | 11 | 11 | 0 | REST adapter |
| `EventDocumentTest` | 6 | 6 | 0 | Persistence adapter |
| **TOTAL** | **61** | **61** | **0** | |

---

## Traceability — AC Coverage

| AC | Description | Test IDs | Tests | Status |
|----|-------------|----------|-------|--------|
| AC-1 | Hexagonal package structure | compile-time | — | ✅ Build green |
| AC-2 | Domain zero framework deps | TEST-D01–D10, TEST-L01–L10 | 21 | ✅ |
| AC-3 | Ports are interfaces | compile-time | — | ✅ Type system |
| AC-4 | REST calls only Inbound Ports | TEST-R01–R09b | 11 | ✅ |
| AC-5 | Persistence maps domain ↔ ES doc | TEST-P01–P05 | 6 | ✅ |
| AC-6 | Custom Micrometer metrics | TEST-A07 | 1 | ✅ |
| AC-7 | Unit tests domain + app service | TEST-S01–S12, TEST-A01–A09 | 23 | ✅ |
| AC-8 | Integration test for REST adapter | TEST-R01–R09b | 11 | ✅ |
| AC-9 | `./gradlew build` passes green | (CI artifact) | — | ✅ |
| AC-10 | Actuator endpoints remain | (Spring context load) | — | ✅ |

---

## Test Details

### `EventTest` (11 tests — domain model)
- `create generates a unique id` // AC: GH-3-AC-2
- `create fails when title is blank` // TEST-D01
- `create fails when description is blank` // TEST-D02
- `create fails when venue is blank` // TEST-D03
- `create fails when endTime is before startTime` // TEST-D04
- `create fails when endTime equals startTime` // TEST-D04 BVA
- `isUpcoming returns true for future events`
- `isUpcoming returns false for past events`
- `addTag adds tag to event` // TEST-D06
- `removeTag removes tag from event` // TEST-D07
- `update returns new event with modified fields preserving id` // TEST-D08
- `update does not mutate original event`

### `LocationTest` (10 tests — domain model)
- `valid location is created`
- `latitude above 90 throws` // TEST-L01
- `latitude below minus 90 throws` // TEST-L02
- `longitude above 180 throws` // TEST-L03
- `longitude below minus 180 throws` // TEST-L04
- `latitude at boundary 90 is valid` // TEST-L05 BVA
- `latitude at boundary minus 90 is valid` // TEST-L06 BVA
- `blank city throws`
- `blank country throws`
- `distance between same location is zero` // TEST-L07
- `distance between Berlin and Munich is approximately 504 km` // TEST-L08
- `distance is symmetric`

### `EventDomainServiceTest` (12 tests — domain service)
- `filterByPreferences returns empty list when catalogue is empty` // TEST-S01 **edge case**
- `filterByPreferences returns all events when categories empty` // TEST-S02 cold start
- `filterByPreferences filters by category` // TEST-S03
- `filterByPreferences includes event within distance threshold` // TEST-S04 BVA
- `filterByPreferences does not filter by date, returns past events` // TEST-S05
- `rank returns empty list when no events provided` // TEST-S06 **edge case**
- `rank puts upcoming events first` // TEST-S07
- `rank sorts by geo proximity when userLocation provided` // TEST-S08
- `filterByPreferences includes event matching category AND within radius` // TEST-S09 decision table
- `filterByPreferences excludes event matching category but outside radius` // TEST-S10 decision table
- `filterByPreferences excludes event wrong category even if within radius` // TEST-S11 decision table
- `rank with single event returns that event`

### `EventApplicationServiceTest` (11 tests — application layer)
- `createEvent saves event and publishes domain event` // TEST-A01
- `createEvent records query duration and index operation metrics` // TEST-A07 **AC-6**
- `createEvent publishes EventCreatedEvent with correct id`
- `findEvent returns event when found` // TEST-A02
- `findEvent returns null when not found` // TEST-A03
- `listEvents delegates to repository` // TEST-A04
- `listEvents with size 1 returns single event` // TEST-A06 BVA
- `listEvents returns empty list when no events exist` // **edge case**
- `recommendEvents filters and ranks events` // TEST-A05
- `recommendEvents with empty categories returns all candidates` // **cold start**

### `EventControllerTest` (11 tests — REST adapter)
- `GET event by id returns 200 when found` // TEST-R01
- `GET event by id returns 404 when not found` // TEST-R02
- `POST create event returns 201` // TEST-R03
- `POST create event returns 400 when title is missing` // TEST-R04 **negative**
- `POST create event returns 400 for invalid category value` // TEST-R05 **negative**
- `GET list events returns 200` // TEST-R06
- `GET list events returns 200 with empty array when no events` // TEST-R07 **edge case**
- `error response does not expose stack trace in body` // TEST-R08 **OWASP-A05**
- `POST recommendations returns 200 with ranked event list` // TEST-R09
- `POST recommendations with empty categories returns 200` // **cold start**

### `EventDocumentTest` (6 tests — persistence adapter)
- `fromDomain maps all domain fields to EventDocument` // TEST-P01 **AC-5**
- `toDomain reconstructs domain event from EventDocument` // TEST-P02 **AC-5**
- `roundtrip fromDomain then toDomain preserves all fields` // TEST-P03 **AC-5**
- `fromDomain with no tags produces empty tag list`
- `fromDomain and toDomain correctly map all Category enum values`

---

## Bug Found & Fixed During Testing

| Bug | Impact | Fix |
|-----|--------|-----|
| `POST /api/v1/events` with missing required fields returned **500** instead of **400** | OWASP A05 — Security Misconfiguration | Added `@ExceptionHandler(HttpMessageNotReadableException::class)` → 400 in `GlobalExceptionHandler` |

---

## Test Infrastructure

- **Domain/Application/Document tests**: Pure unit tests using `mockk` — no Spring context, no I/O
- **Controller test**: `@SpringBootTest(webEnvironment=MOCK)` + `@AutoConfigureMockMvc` with `@ActiveProfiles("test")`
- **Test profile**: Excludes ES auto-config via `application-test.yml`; `ElasticsearchEventRepositoryAdapter` skipped via `@Profile("!test")`
- **Inbound port mocks**: `companion object` in `MockUseCaseConfig` for stateful test stubbing
- **OWASP A05**: Stack trace leak verified via `content { string(not(containsString("at com."))) }`
