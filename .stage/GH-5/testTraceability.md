# GH-5 — Test Traceability Matrix

**Date:** 2025-06-12  
**Ticket:** GH-5 — Local Dev Environment: Kafka + Conduktor integrated into main `docker-compose.yml`

---

## Acceptance Criteria ↔ Test Case Mapping

| AC # | Acceptance Criterion | Test ID | Test Type | Test Name | Status |
|------|---------------------|---------|-----------|-----------|--------|
| AC-1 | Kafka broker is reachable at `localhost:9092` | TC-GH5-01 | Live | Kafka Broker Reachability | Pending |
| AC-2 | Conduktor UI is accessible via `http://localhost:8088` | TC-GH5-02 | Live | Conduktor UI Accessibility | Pending |
| AC-3 | Conduktor is pre-configured to connect to local Kafka broker on startup | TC-GH5-03 | Live | Conduktor Pre-configuration | Pending |
| AC-4 | Event consumer app can be pointed at local broker via environment variable | TC-GH5-04 | Live | Event Consumer App Kafka Configuration | Pending |
| AC-5 | README or inline documentation explains how to start the local stack | TC-GH5-05 | Static | README Content Validation | Pending |
| AC-6 | Named volumes ensure Kafka data persists across container restarts | TC-GH5-06 | Live | Kafka Data Persistence | Pending |
| AC-7 | No port conflicts with main `docker-compose.yml` services | TC-GH5-07 | Static | Port Collision Detection | Pending |
| AC-8 | Zookeeper/KRaft decision is documented in architecture review | TC-GH5-08 | Static | Architecture Review Documentation | Pending |
| AC-9 | All services start and report healthy within 120s | TC-GH5-09 | Live | All Services Start and Report Healthy | Pending |

---

## Test Case ↔ Acceptance Criteria Mapping (Reverse)

| Test ID | Test Name | AC # | AC Description | Coverage |
|---------|-----------|------|---|---|
| TC-GH5-01 | Kafka Broker Reachability | AC-1 | Kafka broker is reachable at `localhost:9092` | ✅ Full |
| TC-GH5-02 | Conduktor UI Accessibility | AC-2 | Conduktor UI is accessible via `http://localhost:8088` | ✅ Full |
| TC-GH5-03 | Conduktor Pre-configuration | AC-3 | Conduktor is pre-configured to connect to local Kafka broker on startup | ✅ Full |
| TC-GH5-04 | Event Consumer App Kafka Configuration | AC-4 | Event consumer app can be pointed at local broker via environment variable | ✅ Full |
| TC-GH5-05 | README Content Validation | AC-5 | README or inline documentation explains how to start the local stack | ✅ Full |
| TC-GH5-06 | Kafka Data Persistence | AC-6 | Named volumes ensure Kafka data persists across container restarts | ✅ Full |
| TC-GH5-07 | Port Collision Detection | AC-7 | No port conflicts with main `docker-compose.yml` services | ✅ Full |
| TC-GH5-08 | Architecture Review Documentation | AC-8 | Zookeeper/KRaft decision is documented in architecture review | ✅ Full |
| TC-GH5-09 | All Services Start and Report Healthy | AC-9 | All services start and report healthy within 120s | ✅ Full |

---

## Supporting Infrastructure Tests (Not Mapped to AC, but Important)

| Test ID | Test Name | Purpose | Type |
|---------|-----------|---------|------|
| TC-GH5-STATIC-01 | YAML Syntax Validation | Prerequisite for all live tests | Static |
| TC-GH5-STATIC-02 | Volume Declaration Completeness | Validates persistence infrastructure | Static |
| TC-GH5-STATIC-03 | Service Dependency Resolution | Validates startup order | Static |
| TC-GH5-STATIC-04 | Healthcheck Syntax Validation | Validates readiness checks | Static |
| TC-GH5-LIVE-01 | Elasticsearch Connectivity | Validates data layer | Live |
| TC-GH5-LIVE-02 | Kibana Connectivity | Validates observability layer | Live |
| TC-GH5-LIVE-03 | APM Server Connectivity | Validates monitoring layer | Live |
| TC-GH5-LIVE-04 | OTel Collector Connectivity | Validates tracing layer | Live |
| TC-GH5-LIVE-05 | Spring Boot App Actuator Health | Validates app layer | Live |

---

## Coverage Summary

| Category | Count | Coverage |
|----------|-------|----------|
| **Acceptance Criteria** | 9 | 9/9 (100%) ✅ |
| **Primary Test Cases** | 9 | 1 test per AC |
| **Supporting Tests** | 9 | Infrastructure quality |
| **Total Tests** | 18 | Comprehensive |

---

## Test Execution Order

### Phase 1: Static Tests (No Docker Boot)
1. TC-GH5-STATIC-01 — YAML Syntax Validation (prerequisite)
2. TC-GH5-STATIC-02 — Volume Declaration Completeness
3. TC-GH5-STATIC-03 — Service Dependency Resolution
4. TC-GH5-STATIC-04 — Healthcheck Syntax Validation
5. TC-GH5-05 — README Content Validation
6. TC-GH5-07 — Port Collision Detection
7. TC-GH5-08 — Architecture Review Documentation

**Expected Duration:** < 30 seconds  
**Failure Handling:** STOP if any test fails; do not proceed to live tests

### Phase 2: Live Tests (Docker Boot Required)
1. TC-GH5-09 — All Services Start and Report Healthy (startup validation)
2. TC-GH5-01 — Kafka Broker Reachability
3. TC-GH5-02 — Conduktor UI Accessibility
4. TC-GH5-03 — Conduktor Pre-configuration
5. TC-GH5-04 — Event Consumer App Kafka Configuration
6. TC-GH5-06 — Kafka Data Persistence
7. TC-GH5-LIVE-01 — Elasticsearch Connectivity
8. TC-GH5-LIVE-02 — Kibana Connectivity
9. TC-GH5-LIVE-03 — APM Server Connectivity
10. TC-GH5-LIVE-04 — OTel Collector Connectivity
11. TC-GH5-LIVE-05 — Spring Boot App Actuator Health

**Expected Duration:** 3–5 minutes (including 120s startup time)  
**Failure Handling:** Document failure with root cause; do not skip tests

### Phase 3: Cleanup
- `docker compose down` (NO `-v` flag to preserve volumes)

---

## Notes

- **100% AC Coverage:** Every acceptance criterion has at least one corresponding test
- **Layered Testing:** Static tests validate structure; live tests validate behavior
- **Comprehensive Infrastructure:** Supporting tests validate all layers (data, observability, monitoring, tracing, app)
- **Persistence Validation:** TC-GH5-06 specifically tests data survival across container restarts
- **Port Isolation:** TC-GH5-07 validates no conflicts with existing services

---

**End of Test Traceability Matrix**
