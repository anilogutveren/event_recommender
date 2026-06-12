# GH-5 — Test Design Document

**Date:** 2025-06-12  
**Phase:** TEST (Test Design)  
**Ticket:** GH-5 — Local Dev Environment: Kafka + Conduktor integrated into main `docker-compose.yml`  
**Test Framework:** Bash + Docker Compose (shell-based infrastructure validation)

---

## Executive Summary

This document defines **systematic test cases** for GH-5's infrastructure changes. Since this ticket is **infrastructure-only** (no Kotlin source code modified), the test strategy is **static validation + live smoke tests** of the integrated Docker Compose stack.

**TDD Path:** Skipped — infrastructure-only ticket; code already implemented and architecturally reviewed. Tests are designed to **validate** the implementation, not drive it.

---

## Test Strategy

### Test Categories

1. **Static Tests** (no Docker boot required)
   - YAML syntax validation
   - Port collision detection
   - Volume declaration completeness
   - Service dependency resolution
   - Healthcheck syntax validation
   - README content checks

2. **Live Tests** (requires `docker compose up`)
   - Stack boots without errors within 120s timeout per service
   - All services report `healthy` status
   - Each endpoint responds correctly:
     - Spring Boot app: `GET /actuator/health`
     - Elasticsearch: `GET /_cluster/health`
     - Kibana: `GET /api/status`
     - Conduktor: `GET /` (any 2xx/3xx acceptable)
     - APM Server: `GET /`
     - OTel Collector: `GET /` (health endpoint)
     - Kafka: `kafka-broker-api-versions --bootstrap-server localhost:9092`
   - Kafka persistence: create topic, write message, restart kafka service, verify topic exists
   - Conduktor integration: verify Conduktor sees the local-kafka cluster

---

## Acceptance Criteria Mapping

| AC # | Acceptance Criterion | Test ID | Technique | Type |
|------|---------------------|---------|-----------|------|
| AC-1 | Kafka broker is reachable at `localhost:9092` | TC-GH5-01 | Happy path + sad path | Live |
| AC-2 | Conduktor UI is accessible via `http://localhost:8088` | TC-GH5-02 | Happy path + sad path | Live |
| AC-3 | Conduktor is pre-configured to connect to local Kafka broker on startup | TC-GH5-03 | State transition | Live |
| AC-4 | Event consumer app can be pointed at local broker via environment variable | TC-GH5-04 | Happy path | Live |
| AC-5 | README or inline documentation explains how to start the local stack | TC-GH5-05 | Content validation | Static |
| AC-6 | Named volumes ensure Kafka data persists across container restarts | TC-GH5-06 | State transition | Live |
| AC-7 | No port conflicts with main `docker-compose.yml` services | TC-GH5-07 | Equivalence partitioning | Static |
| AC-8 | Zookeeper/KRaft decision is documented in architecture review | TC-GH5-08 | Content validation | Static |
| AC-9 | All services start and report healthy within 120s | TC-GH5-09 | Happy path + boundary | Live |

---

## Test Cases

### STATIC TESTS (No Docker Boot)

---

#### **TC-GH5-05: README Content Validation**

| Field | Value |
|-------|-------|
| **Test ID** | TC-GH5-05 |
| **Linked AC** | AC-5: README or inline documentation explains how to start the local stack |
| **Technique** | Content validation |
| **Priority** | P1 |
| **Type** | Static |
| **Preconditions** | README.md exists in project root |
| **Steps** | 1. Read README.md<br>2. Verify "Local Development Stack" section exists<br>3. Verify "Quick Start" subsection with `docker compose up` command<br>4. Verify service endpoints table includes all 8 services<br>5. Verify Kafka configuration section (KRaft, internal/external listeners)<br>6. Verify Conduktor instructions (URL, admin credentials)<br>7. Verify reset procedure (`docker compose down -v`)<br>8. Verify CLI examples (kcat, kafka-console-producer)<br>9. Verify troubleshooting section |
| **Expected Result** | All sections present; documentation is clear and copy-paste-ready |
| **Notes** | Validates AC-5 (documentation requirement) |

---

#### **TC-GH5-07: Port Collision Detection**

| Field | Value |
|-------|-------|
| **Test ID** | TC-GH5-07 |
| **Linked AC** | AC-7: No port conflicts with main `docker-compose.yml` services |
| **Technique** | Equivalence partitioning (port ranges) |
| **Priority** | P1 |
| **Type** | Static |
| **Preconditions** | docker-compose.yml exists; Docker Compose CLI available |
| **Steps** | 1. Run `docker compose config` to validate YAML<br>2. Extract all published ports from compose output<br>3. Check for duplicates in port list<br>4. Verify expected ports: 8080 (app), 9200 (ES), 5601 (Kibana), 9092 (Kafka), 8088 (Conduktor), 8200 (APM), 4317 (OTel gRPC), 4318 (OTel HTTP)<br>5. Verify no unexpected ports are exposed |
| **Expected Result** | All ports unique; no collisions detected; expected ports present |
| **Notes** | Validates AC-7 (port isolation) |

---

#### **TC-GH5-08: Architecture Review Documentation**

| Field | Value |
|-------|-------|
| **Test ID** | TC-GH5-08 |
| **Linked AC** | AC-8: Zookeeper/KRaft decision is documented in architecture review |
| **Technique** | Content validation |
| **Priority** | P1 |
| **Type** | Static |
| **Preconditions** | ADR-0004 exists at `docs/adr/infrastructure/ADR-0004-local-kafka-conduktor-stack.md` |
| **Steps** | 1. Read ADR-0004<br>2. Verify "Decision" section exists<br>3. Verify KRaft is chosen (not Zookeeper)<br>4. Verify rationale for KRaft selection<br>5. Verify Kafka image choice (Confluent 7.x)<br>6. Verify Conduktor edition choice (free tier)<br>7. Verify port mapping strategy documented<br>8. Verify hexagonal architecture alignment section |
| **Expected Result** | ADR-0004 documents KRaft decision with full rationale; all design choices justified |
| **Notes** | Validates AC-8 (architecture documentation) |

---

#### **TC-GH5-STATIC-01: YAML Syntax Validation**

| Field | Value |
|-------|-------|
| **Test ID** | TC-GH5-STATIC-01 |
| **Linked AC** | (Infrastructure quality) |
| **Technique** | Static analysis |
| **Priority** | P1 |
| **Type** | Static |
| **Preconditions** | docker-compose.yml exists; Docker Compose CLI available |
| **Steps** | 1. Run `docker compose config > /dev/null 2>&1`<br>2. Capture exit code |
| **Expected Result** | Exit code 0 (YAML is valid) |
| **Notes** | Prerequisite for all live tests; must pass before proceeding |

---

#### **TC-GH5-STATIC-02: Volume Declaration Completeness**

| Field | Value |
|-------|-------|
| **Test ID** | TC-GH5-STATIC-02 |
| **Linked AC** | AC-6: Named volumes ensure Kafka data persists |
| **Technique** | Equivalence partitioning |
| **Priority** | P1 |
| **Type** | Static |
| **Preconditions** | docker-compose.yml exists; Docker Compose CLI available |
| **Steps** | 1. Run `docker compose config` and extract volumes section<br>2. Verify `elasticsearch-data` volume declared<br>3. Verify `kafka-data` volume declared<br>4. Verify `conduktor-postgres-data` volume declared<br>5. Verify each volume is mounted in corresponding service<br>6. Verify mount paths are correct (e.g., `/var/lib/kafka/data` for kafka-data) |
| **Expected Result** | All three named volumes declared and mounted correctly |
| **Notes** | Validates AC-6 (persistence) and NFR-1 |

---

#### **TC-GH5-STATIC-03: Service Dependency Resolution**

| Field | Value |
|-------|-------|
| **Test ID** | TC-GH5-STATIC-03 |
| **Linked AC** | (Infrastructure quality) |
| **Technique** | Equivalence partitioning |
| **Priority** | P1 |
| **Type** | Static |
| **Preconditions** | docker-compose.yml exists; Docker Compose CLI available |
| **Steps** | 1. Run `docker compose config` and extract depends_on sections<br>2. For each service with depends_on, verify all referenced services exist<br>3. Verify no circular dependencies<br>4. Verify health check conditions are present on critical services (elasticsearch, kafka, conduktor-postgres) |
| **Expected Result** | All service references valid; no circular dependencies; health checks present on critical services |
| **Notes** | Validates service startup order and readiness checks |

---

#### **TC-GH5-STATIC-04: Healthcheck Syntax Validation**

| Field | Value |
|-------|-------|
| **Test ID** | TC-GH5-STATIC-04 |
| **Linked AC** | (Infrastructure quality) |
| **Technique** | Static analysis |
| **Priority** | P2 |
| **Type** | Static |
| **Preconditions** | docker-compose.yml exists; Docker Compose CLI available |
| **Steps** | 1. Run `docker compose config` and extract healthcheck sections<br>2. For each service with healthcheck, verify:<br>   - `test` field is present and non-empty<br>   - `interval` is a valid duration (e.g., 10s)<br>   - `timeout` is a valid duration<br>   - `retries` is a positive integer |
| **Expected Result** | All healthchecks have valid syntax; no malformed durations or missing fields |
| **Notes** | Validates healthcheck configuration quality |

---

### LIVE TESTS (Requires `docker compose up`)

---

#### **TC-GH5-01: Kafka Broker Reachability**

| Field | Value |
|-------|-------|
| **Test ID** | TC-GH5-01 |
| **Linked AC** | AC-1: Kafka broker is reachable at `localhost:9092` |
| **Technique** | Happy path + sad path |
| **Priority** | P1 |
| **Type** | Live |
| **Preconditions** | `docker compose up -d` has completed; all services are healthy |
| **Steps** | **Happy path:**<br>1. Run `docker exec event-recommender-kafka kafka-broker-api-versions --bootstrap-server localhost:9092`<br>2. Verify exit code is 0<br>3. Verify output contains broker metadata<br><br>**Sad path:**<br>4. Verify port 9092 is listening on host: `lsof -i :9092` or `netstat -an \| grep 9092`<br>5. Verify connection from host: `nc -zv localhost 9092` (or `telnet localhost 9092`) |
| **Expected Result** | **Happy path:** Exit code 0; broker responds to metadata requests<br>**Sad path:** Port 9092 is listening; connection succeeds |
| **Notes** | Validates AC-1 (Kafka reachability); tests both internal (Docker) and external (host) connectivity |

---

#### **TC-GH5-02: Conduktor UI Accessibility**

| Field | Value |
|-------|-------|
| **Test ID** | TC-GH5-02 |
| **Linked AC** | AC-2: Conduktor UI is accessible via `http://localhost:8088` |
| **Technique** | Happy path + sad path |
| **Priority** | P1 |
| **Type** | Live |
| **Preconditions** | `docker compose up -d` has completed; all services are healthy |
| **Steps** | **Happy path:**<br>1. Run `curl -s -o /dev/null -w "%{http_code}" http://localhost:8088/`<br>2. Verify HTTP status code is 2xx or 3xx (200, 301, 302, etc.)<br>3. Run `curl -s http://localhost:8088/ \| grep -i "conduktor"` to verify response contains Conduktor branding<br><br>**Sad path:**<br>4. Verify port 8088 is listening: `lsof -i :8088`<br>5. Verify connection succeeds: `nc -zv localhost 8088` |
| **Expected Result** | **Happy path:** HTTP 2xx/3xx response; Conduktor UI is accessible<br>**Sad path:** Port 8088 is listening; connection succeeds |
| **Notes** | Validates AC-2 (Conduktor accessibility); note port is 8088 (not 8080 per original plan) |

---

#### **TC-GH5-03: Conduktor Pre-configuration**

| Field | Value |
|-------|-------|
| **Test ID** | TC-GH5-03 |
| **Linked AC** | AC-3: Conduktor is pre-configured to connect to local Kafka broker on startup |
| **Technique** | State transition |
| **Priority** | P1 |
| **Type** | Live |
| **Preconditions** | `docker compose up -d` has completed; Conduktor service is healthy |
| **Steps** | 1. Wait for Conduktor to be healthy (health check passes)<br>2. Check Conduktor logs for cluster registration: `docker compose logs conduktor \| grep -i "cluster\|kafka\|connected"`<br>3. Query Conduktor API (if available) to list configured clusters: `curl -s http://localhost:8088/api/clusters` (may require auth)<br>4. Verify Conduktor environment variables are set correctly: `docker compose config \| grep -A 10 "conduktor:"`<br>5. Verify `CDK_CLUSTERS_0_BOOTSTRAPSERVERS` is set to `kafka:29092` |
| **Expected Result** | Conduktor logs show successful cluster registration; environment variables are correct; Conduktor is ready to inspect Kafka topics |
| **Notes** | Validates AC-3 (pre-configuration); tests zero-config startup for developers |

---

#### **TC-GH5-04: Event Consumer App Kafka Configuration**

| Field | Value |
|-------|-------|
| **Test ID** | TC-GH5-04 |
| **Linked AC** | AC-4: Event consumer app can be pointed at local broker via environment variable |
| **Technique** | Happy path |
| **Priority** | P1 |
| **Type** | Live |
| **Preconditions** | `docker compose up -d` has completed; app service is healthy |
| **Steps** | 1. Check app service environment variables: `docker compose config \| grep -A 5 "app:"`<br>2. Verify `KAFKA_BOOTSTRAP_SERVERS` is set to `kafka:29092`<br>3. Check app logs for Kafka connection: `docker compose logs app \| grep -i "kafka\|bootstrap"`<br>4. Verify app service depends_on Kafka with health check condition |
| **Expected Result** | App service has `KAFKA_BOOTSTRAP_SERVERS=kafka:29092`; app logs show successful Kafka connection; app depends_on Kafka with health check |
| **Notes** | Validates AC-4 (environment variable override); tests app can connect to local broker |

---

#### **TC-GH5-06: Kafka Data Persistence**

| Field | Value |
|-------|-------|
| **Test ID** | TC-GH5-06 |
| **Linked AC** | AC-6: Named volumes ensure Kafka data persists across container restarts |
| **Technique** | State transition |
| **Priority** | P1 |
| **Type** | Live |
| **Preconditions** | `docker compose up -d` has completed; all services are healthy |
| **Steps** | 1. Create a test topic: `docker exec event-recommender-kafka kafka-topics --bootstrap-server localhost:9092 --create --topic test-persistence --partitions 1 --replication-factor 1`<br>2. Verify topic exists: `docker exec event-recommender-kafka kafka-topics --bootstrap-server localhost:9092 --list \| grep test-persistence`<br>3. Write a test message: `echo "test-message" \| docker exec -i event-recommender-kafka kafka-console-producer --broker-list localhost:9092 --topic test-persistence`<br>4. Stop Kafka service: `docker compose stop kafka`<br>5. Start Kafka service: `docker compose start kafka`<br>6. Wait for Kafka to be healthy (health check passes)<br>7. Verify topic still exists: `docker exec event-recommender-kafka kafka-topics --bootstrap-server localhost:9092 --list \| grep test-persistence`<br>8. Verify message is still there: `docker exec event-recommender-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic test-persistence --from-beginning --max-messages 1` |
| **Expected Result** | Topic persists after restart; message is still readable; named volume `kafka-data` preserved data |
| **Notes** | Validates AC-6 (persistence) and NFR-1 (named volumes); tests data survival across container restarts |

---

#### **TC-GH5-09: All Services Start and Report Healthy**

| Field | Value |
|-------|-------|
| **Test ID** | TC-GH5-09 |
| **Linked AC** | AC-9: All services start and report healthy within 120s |
| **Technique** | Happy path + boundary value analysis |
| **Priority** | P1 |
| **Type** | Live |
| **Preconditions** | Docker Compose CLI available; no services currently running |
| **Steps** | 1. Start stack: `docker compose up -d`<br>2. Record start time<br>3. Poll `docker compose ps --format json` every 5s for up to 240s<br>4. For each service, check:<br>   - Container is `running` state<br>   - Health status is `healthy` (if healthcheck defined)<br>5. Record time when all services are healthy<br>6. Verify total time is ≤ 120s (target) or ≤ 240s (hard limit)<br>7. Check for any errors in logs: `docker compose logs \| grep -i "error\|fatal\|exception"` |
| **Expected Result** | All services start within 120s; all services report healthy; no errors in logs |
| **Notes** | Validates AC-9 (startup time); tests both happy path (all services healthy) and boundary (120s timeout) |

---

#### **TC-GH5-LIVE-01: Elasticsearch Connectivity**

| Field | Value |
|-------|-------|
| **Test ID** | TC-GH5-LIVE-01 |
| **Linked AC** | (Infrastructure quality) |
| **Technique** | Happy path |
| **Priority** | P2 |
| **Type** | Live |
| **Preconditions** | `docker compose up -d` has completed; all services are healthy |
| **Steps** | 1. Run `curl -s http://localhost:9200/_cluster/health`<br>2. Verify HTTP 200 response<br>3. Verify response contains `"status":"green"` or `"status":"yellow"` |
| **Expected Result** | Elasticsearch responds to health check; cluster is operational |
| **Notes** | Validates Elasticsearch is reachable and healthy |

---

#### **TC-GH5-LIVE-02: Kibana Connectivity**

| Field | Value |
|-------|-------|
| **Test ID** | TC-GH5-LIVE-02 |
| **Linked AC** | (Infrastructure quality) |
| **Technique** | Happy path |
| **Priority** | P2 |
| **Type** | Live |
| **Preconditions** | `docker compose up -d` has completed; all services are healthy |
| **Steps** | 1. Run `curl -s http://localhost:5601/api/status`<br>2. Verify HTTP 200 response<br>3. Verify response contains `"state":"green"` or similar status indicator |
| **Expected Result** | Kibana responds to status check; UI is operational |
| **Notes** | Validates Kibana is reachable and healthy |

---

#### **TC-GH5-LIVE-03: APM Server Connectivity**

| Field | Value |
|-------|-------|
| **Test ID** | TC-GH5-LIVE-03 |
| **Linked AC** | (Infrastructure quality) |
| **Technique** | Happy path |
| **Priority** | P2 |
| **Type** | Live |
| **Preconditions** | `docker compose up -d` has completed; all services are healthy |
| **Steps** | 1. Run `curl -s http://localhost:8200/`<br>2. Verify HTTP 200 or 202 response |
| **Expected Result** | APM Server responds; service is operational |
| **Notes** | Validates APM Server is reachable and healthy |

---

#### **TC-GH5-LIVE-04: OTel Collector Connectivity**

| Field | Value |
|-------|-------|
| **Test ID** | TC-GH5-LIVE-04 |
| **Linked AC** | (Infrastructure quality) |
| **Technique** | Happy path |
| **Priority** | P2 |
| **Type** | Live |
| **Preconditions** | `docker compose up -d` has completed; all services are healthy |
| **Steps** | 1. Run `curl -s http://localhost:13133/` (OTel health endpoint)<br>2. Verify HTTP 200 response |
| **Expected Result** | OTel Collector responds to health check; service is operational |
| **Notes** | Validates OTel Collector is reachable and healthy |

---

#### **TC-GH5-LIVE-05: Spring Boot App Actuator Health**

| Field | Value |
|-------|-------|
| **Test ID** | TC-GH5-LIVE-05 |
| **Linked AC** | (Infrastructure quality) |
| **Technique** | Happy path |
| **Priority** | P2 |
| **Type** | Live |
| **Preconditions** | `docker compose up -d` has completed; all services are healthy |
| **Steps** | 1. Run `curl -s http://localhost:8080/actuator/health`<br>2. Verify HTTP 200 response<br>3. Verify response contains `"status":"UP"` or similar |
| **Expected Result** | Spring Boot app responds to health check; app is operational |
| **Notes** | Validates Spring Boot app is reachable and healthy |

---

## Test Execution Plan

### Phase 1: Static Tests (No Docker Boot)
1. Run all `TC-GH5-STATIC-*` and `TC-GH5-05`, `TC-GH5-07`, `TC-GH5-08` tests
2. Expected duration: < 30 seconds
3. If any static test fails, **STOP** and report failure before proceeding to live tests

### Phase 2: Live Tests (Docker Boot Required)
1. Verify no services are currently running: `docker compose ps`
2. Verify all required ports are free: `lsof -i :8080`, `lsof -i :9092`, etc.
3. Run `docker compose up -d` to start the stack
4. Run all `TC-GH5-*` live tests in order
5. Expected duration: 3–5 minutes (including 120s startup time)
6. On completion, run `docker compose down` (without `-v` to preserve volumes)

### Phase 3: Cleanup
1. Run `docker compose down` (no `-v` flag)
2. Verify all containers are stopped: `docker compose ps`
3. Verify volumes are preserved: `docker volume ls \| grep event-recommender`

---

## Test Traceability Matrix

| AC # | AC Description | Test ID | Status |
|------|---|---|---|
| AC-1 | Kafka broker is reachable at `localhost:9092` | TC-GH5-01 | Pending |
| AC-2 | Conduktor UI is accessible via `http://localhost:8088` | TC-GH5-02 | Pending |
| AC-3 | Conduktor is pre-configured to connect to local Kafka broker on startup | TC-GH5-03 | Pending |
| AC-4 | Event consumer app can be pointed at local broker via environment variable | TC-GH5-04 | Pending |
| AC-5 | README or inline documentation explains how to start the local stack | TC-GH5-05 | Pending |
| AC-6 | Named volumes ensure Kafka data persists across container restarts | TC-GH5-06 | Pending |
| AC-7 | No port conflicts with main `docker-compose.yml` services | TC-GH5-07 | Pending |
| AC-8 | Zookeeper/KRaft decision is documented in architecture review | TC-GH5-08 | Pending |
| AC-9 | All services start and report healthy within 120s | TC-GH5-09 | Pending |

---

## Notes

- **TDD Path:** Skipped — infrastructure-only ticket; code already implemented and architecturally reviewed
- **Test Framework:** Bash + Docker Compose (shell-based infrastructure validation) — no Kotlin/JUnit tests
- **Live Test Timeout:** 240s per service health check (120s target); if exceeded, test is marked BLOCKED
- **Port Binding:** If any required port is already bound, live tests are marked BLOCKED with reason
- **Cleanup:** `docker compose down` (NO `-v` flag) is mandatory on exit to preserve volumes for manual inspection

---

**End of Test Design Document**
