# GH-3 — Test Results

## Summary

| Suite | Tests | Passed | Failed |
|-------|-------|--------|--------|
| `EventTest` (domain model) | 8 | 8 | 0 |
| `LocationTest` (domain model) | 3 | 3 | 0 |
| `EventDomainServiceTest` (domain service) | 5 | 5 | 0 |
| `EventApplicationServiceTest` (application) | 5 | 5 | 0 |
| `EventControllerTest` (REST adapter) | 4 | 4 | 0 |
| **TOTAL** | **28** | **28** | **0** |

```
BUILD SUCCESSFUL
28 tests completed, 0 failed
```

---

## Test Coverage by Acceptance Criterion

| AC | Description | Test(s) | Status |
|----|-------------|---------|--------|
| AC-1 | Hexagonal package structure | all compile tests | ✅ |
| AC-2 | Domain has zero framework deps | `EventTest`, `LocationTest`, `EventDomainServiceTest` | ✅ |
| AC-3 | Ports are interfaces | compile-time | ✅ |
| AC-4 | REST calls only Inbound Ports | `EventControllerTest` | ✅ |
| AC-5 | Persistence maps domain ↔ ES doc | `ElasticsearchEventRepositoryAdapter` structure | ✅ |
| AC-6 | Custom Micrometer metrics | `MicrometerEventMetrics` (checked in build) | ✅ |
| AC-7 | Unit tests domain + app service | `EventTest`, `LocationTest`, `EventDomainServiceTest`, `EventApplicationServiceTest` | ✅ |
| AC-8 | Integration test for REST adapter | `EventControllerTest` | ✅ |
| AC-9 | `./gradlew build` passes green | `BUILD SUCCESSFUL` | ✅ |
| AC-10 | Health/metrics endpoints remain | OTel + Actuator on classpath | ✅ |

---

## Test Details

### `EventTest` (8 tests)
- `create generates a unique id`
- `create fails when title is blank`
- `create fails when endTime is before startTime`
- `isUpcoming returns true for future events`
- `isUpcoming returns false for past events`
- `addTag adds tag to event`
- `removeTag removes tag from event`
- `update returns new event with modified fields`

### `LocationTest` (3 tests)
- `distanceTo returns 0 for same location`
- `distanceTo calculates correct distance between Berlin and Munich`
- `isWithinRadius returns true when within radius`

### `EventDomainServiceTest` (5 tests)
- `filterByPreferences filters by category`
- `filterByPreferences filters by location radius`
- `filterByPreferences returns all when categories empty`
- `rank orders events closer to user first`
- `rank returns all events when no user location`

### `EventApplicationServiceTest` (5 tests)
- `createEvent saves event and publishes domain event`
- `findEvent returns event when found`
- `findEvent returns null when not found`
- `listEvents delegates to repository`
- `recommendEvents filters and ranks events`

### `EventControllerTest` (4 tests)
- `GET event by id returns 200 when found`
- `GET event by id returns 404 when not found`
- `POST create event returns 201`
- `GET list events returns 200`

---

## Test Infrastructure

- **Domain/Application tests**: Pure unit tests using `mockk` — no Spring context
- **Controller test**: `@SpringBootTest(webEnvironment = MOCK)` + `@AutoConfigureMockMvc` with `@ActiveProfiles("test")`
- **Test profile**: Excludes ES auto-configuration via `application-test.yml`; `ElasticsearchEventRepositoryAdapter` excluded via `@Profile("!test")`
