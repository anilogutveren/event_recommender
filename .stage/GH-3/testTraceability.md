# GH-3 — Test Traceability Matrix

**Date:** 2026-06-02

---

## Requirement → Test Mapping

| AC | Description | Test Class | Test IDs | Status |
|----|-------------|------------|----------|--------|
| AC-1 | Hexagonal package structure | (compile-time) | — | ✅ Build passes |
| AC-2 | Domain zero framework deps | `EventTest`, `LocationTest` | TEST-D01–D08, TEST-L01–L09 | ✅ + gaps filled |
| AC-3 | Ports are interfaces | (compile-time) | — | ✅ Type system |
| AC-4 | REST calls only Inbound Ports | `EventControllerTest` | TEST-R01–R10 | ✅ + gaps filled |
| AC-5 | Persistence maps domain ↔ ES doc | `EventDocumentTest` (new) | TEST-P01–P04 | To be added |
| AC-6 | Custom Micrometer metrics | `EventApplicationServiceTest` | TEST-A07 | To be added |
| AC-7 | Unit tests domain + app service | `EventDomainServiceTest`, `EventApplicationServiceTest` | TEST-S01–S11, TEST-A01–A07 | ✅ + gaps filled |
| AC-8 | Integration test for REST adapter | `EventControllerTest` | TEST-R01–R10 | ✅ + gaps filled |
| AC-9 | `./gradlew build` green | (CI) | — | ✅ BUILD SUCCESSFUL |
| AC-10 | Health/metrics endpoints | `ActuatorTest` (new) | TEST-R10 | To be added |

---

## Test ID → Source Mapping

| Test ID | Test Name | File | AC |
|---------|-----------|------|----|
| TEST-D01 | `create fails when title is blank` | `EventTest` | AC-2 |
| TEST-D02 | `create fails when description is blank` | `EventTest` (new) | AC-2 |
| TEST-D03 | `create fails when venue is blank` | `EventTest` (new) | AC-2 |
| TEST-D04 | `create fails when endTime equals startTime` | `EventTest` (new) | AC-2 |
| TEST-D05 | `create generates a unique id` | `EventTest` | AC-2 |
| TEST-D06 | `addTag adds tag to event` | `EventTest` | AC-2 |
| TEST-D07 | `removeTag removes tag from event` | `EventTest` | AC-2 |
| TEST-D08 | `update returns new event with modified fields` | `EventTest` | AC-2 |
| TEST-L01 | `invalid latitude throws` (91) | `LocationTest` | AC-2 |
| TEST-L02 | `invalid latitude throws` (-91) | `LocationTest` (new) | AC-2 |
| TEST-L03 | `invalid longitude throws` (181) | `LocationTest` | AC-2 |
| TEST-L04 | `invalid longitude throws` (-181) | `LocationTest` (new) | AC-2 |
| TEST-L05 | `latitude boundary 90 is valid` | `LocationTest` (new) | AC-2 |
| TEST-L06 | `latitude boundary -90 is valid` | `LocationTest` (new) | AC-2 |
| TEST-L07 | `distance between same location is zero` | `LocationTest` | AC-2 |
| TEST-L08 | `distance between Berlin and Munich is ~504 km` | `LocationTest` | AC-2 |
| TEST-L09 | `isWithinRadius returns true when within radius` | `LocationTest` (new) | AC-2 |
| TEST-S01 | `filterByPreferences returns empty list when events list is empty` | `EventDomainServiceTest` (new) | AC-7 |
| TEST-S02 | `filterByPreferences returns all events when categories empty` | `EventDomainServiceTest` | AC-7 |
| TEST-S03 | `filterByPreferences filters by category` | `EventDomainServiceTest` | AC-7 |
| TEST-S04 | `filterByPreferences filters by location radius` | `EventDomainServiceTest` | AC-7 |
| TEST-S05 | `filterByPreferences returns past events (no date filter)` | `EventDomainServiceTest` (new) | AC-7 |
| TEST-S06 | `rank returns empty list when no events` | `EventDomainServiceTest` (new) | AC-7 |
| TEST-S07 | `rank puts upcoming events first` | `EventDomainServiceTest` | AC-7 |
| TEST-S08 | `rank sorts by geo proximity when userLocation provided` | `EventDomainServiceTest` | AC-7 |
| TEST-S09 | `filterByPreferences includes event matching category AND within radius` | `EventDomainServiceTest` (new) | AC-7 |
| TEST-S10 | `filterByPreferences excludes event matching category but outside radius` | `EventDomainServiceTest` (new) | AC-7 |
| TEST-S11 | `filterByPreferences excludes event wrong category even if within radius` | `EventDomainServiceTest` (new) | AC-7 |
| TEST-A01 | `createEvent saves event and publishes domain event` | `EventApplicationServiceTest` | AC-7 |
| TEST-A02 | `findEvent returns event when found` | `EventApplicationServiceTest` | AC-7 |
| TEST-A03 | `findEvent returns null when not found` | `EventApplicationServiceTest` | AC-7 |
| TEST-A04 | `listEvents delegates to repository` | `EventApplicationServiceTest` | AC-7 |
| TEST-A05 | `recommendEvents filters and ranks events` | `EventApplicationServiceTest` | AC-7 |
| TEST-A06 | `listEvents with size 1 returns single event` | `EventApplicationServiceTest` (new) | AC-7 |
| TEST-A07 | `createEvent records metrics` | `EventApplicationServiceTest` (new) | AC-6 |
| TEST-R01 | `GET event by id returns 200 when found` | `EventControllerTest` | AC-4, AC-8 |
| TEST-R02 | `GET event by id returns 404 when not found` | `EventControllerTest` | AC-4, AC-8 |
| TEST-R03 | `POST create event returns 201` | `EventControllerTest` | AC-4, AC-8 |
| TEST-R04 | `POST create event returns 400 for invalid JSON` | `EventControllerTest` (new) | AC-8 |
| TEST-R05 | `POST create event returns 400 for invalid category` | `EventControllerTest` (new) | AC-8 |
| TEST-R06 | `GET list events returns 200` | `EventControllerTest` | AC-4, AC-8 |
| TEST-R07 | `GET list events returns empty array when no events` | `EventControllerTest` (new) | AC-8 |
| TEST-R08 | `error response does not expose stack trace` | `EventControllerTest` (new) | AC-8, OWASP-A05 |
| TEST-R09 | `POST recommendations returns 200` | `EventControllerTest` (new) | AC-4, AC-8 |
| TEST-P01 | `EventDocument fromDomain maps all fields` | `EventDocumentTest` (new) | AC-5 |
| TEST-P02 | `EventDocument toDomain maps all fields` | `EventDocumentTest` (new) | AC-5 |
| TEST-P03 | `EventDocument roundtrip preserves data` | `EventDocumentTest` (new) | AC-5 |
