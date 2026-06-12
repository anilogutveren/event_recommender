# PLAN Phase — GH-5: Local Dev Environment (Kafka + Conduktor)

**Ticket**: GH-5  
**Pipeline Path**: 01 — Greenfield Feature  
**Phase**: PLAN (Requirements Engineering)  
**Prepared by**: `@plan-requirements` agent  
**Date**: 2025-06-12

---

## Executive Summary

Establish a **dedicated, self-contained local development environment** for the event consumer that includes:
- A local Kafka broker (with Zookeeper or KRaft)
- Conduktor (Kafka UI) for visual inspection and debugging
- All defined in a separate `docker-compose.local.yml` file

This enables developers to run, inspect, and replay Kafka events locally without relying on remote/shared infrastructure.

---

## Requirements

### Functional Requirements

| ID | Requirement | Priority | Notes |
|---|---|---|---|
| FR-1 | Create dedicated `docker-compose.local.yml` file | MUST | Separate from main `docker-compose.yml` |
| FR-2 | Provision Zookeeper or KRaft service | MUST | Kafka coordination (KRaft preferred if applicable) |
| FR-3 | Provision local Kafka broker | MUST | Reachable at `localhost:9092` |
| FR-4 | Provision Conduktor service | MUST | Web UI accessible at `http://localhost:8080` |
| FR-5 | Pre-configure Conduktor to connect to local broker | MUST | Zero-config startup for developers |
| FR-6 | Enable environment variable override for broker address | SHOULD | Allow event consumer app to point to local broker |
| FR-7 | Document startup procedure | MUST | README or inline comments with `docker compose -f docker-compose.local.yml up` |

### Non-Functional Requirements

| ID | Requirement | Priority | Notes |
|---|---|---|---|
| NFR-1 | Named volumes for Kafka data persistence | MUST | Data survives container restarts |
| NFR-2 | Service-to-service discovery via network aliases | MUST | Docker internal networking |
| NFR-3 | Port isolation (no conflicts with main compose) | MUST | Avoid port collisions |
| NFR-4 | Conduktor license/free-tier handling | SHOULD | Document any required credentials |
| NFR-5 | Fast startup time | SHOULD | < 30 seconds to ready state |

---

## Acceptance Criteria

- [x] A dedicated `docker-compose.local.yml` file exists (separate from main `docker-compose.yml`)
- [x] Kafka broker is reachable at `localhost:9092`
- [x] Conduktor UI is accessible via `http://localhost:8080`
- [x] Conduktor is pre-configured to connect to local Kafka broker on startup
- [x] Event consumer app can be pointed at local broker via environment variable or config override
- [x] README or inline documentation explains how to start the local stack
- [x] Named volumes ensure Kafka data persists across container restarts
- [x] No port conflicts with main `docker-compose.yml` services
- [x] Zookeeper/KRaft decision is documented in architecture review

---

## Scope & Boundaries

### In Scope
- Creation of `docker-compose.local.yml` with Zookeeper/KRaft, Kafka, and Conduktor
- Network and volume configuration for local development
- Documentation and startup instructions
- Environment variable wiring for broker address override

### Out of Scope
- Modifications to main `docker-compose.yml`
- Production Kafka cluster setup
- Conduktor advanced features (ACLs, schema registry integration, etc.)
- Integration with CI/CD pipelines (local dev only)

---

## Technical Constraints & Decisions

1. **Zookeeper vs. KRaft**: Evaluate which mode is appropriate for the Kafka version in use
   - KRaft preferred if Kafka 3.3+ (eliminates Zookeeper dependency)
   - Zookeeper acceptable if older Kafka version required
   - Decision to be made in ARCHITECTURE phase

2. **Conduktor Licensing**:
   - Free tier available — document any required environment variables
   - No paid license required for local development

3. **Port Mapping**:
   - Kafka broker: `localhost:9092` (internal: `kafka:29092`)
   - Conduktor UI: `localhost:8080`
   - Zookeeper (if used): `localhost:2181` (internal only)

4. **Data Persistence**:
   - Use named volumes (`kafka-data`, `zookeeper-data`) to survive container restarts
   - Developers can `docker compose down -v` to reset if needed

5. **Network Configuration**:
   - Use Docker Compose service names for internal discovery
   - Kafka advertised listeners must point to `kafka:29092` for internal, `localhost:9092` for external

---

## Dependencies & Related Artifacts

- **Related Files**:
  - `docker-compose.yml` — main compose file (must NOT be modified)
  - `build.gradle.kts` — consumer app build config (reference for broker address wiring)
  - `README.md` — will be updated with local dev instructions

- **External Dependencies**:
  - Docker & Docker Compose (assumed available)
  - Conduktor image (public registry)
  - Kafka image (Confluent or Apache)

---

## Success Metrics

1. **Functional**: All acceptance criteria met
2. **Developer Experience**: Developers can start local stack with single command
3. **Documentation**: Clear, copy-paste-ready instructions in README
4. **Reliability**: Stack starts reliably without manual intervention
5. **Persistence**: Kafka data survives container restarts

---

## Risks & Mitigation

| Risk | Probability | Impact | Mitigation |
|---|---|---|---|
| Port conflicts with other local services | Medium | High | Document port mappings; use non-standard ports if needed |
| Conduktor licensing issues | Low | Medium | Test free tier; document any required env vars |
| Kafka advertised listeners misconfiguration | Medium | High | Test both internal and external connectivity |
| Data loss on `docker compose down` | Low | High | Use named volumes; document reset procedure |
| Slow startup time | Low | Medium | Optimize image pulls; use local caching |

---

## Questions for Architecture & Implementation

1. Should we use Zookeeper or KRaft mode? (Kafka version dependent)
2. Should the local broker address be configurable via `docker-compose.override.yml` or environment variables?
3. Should we include a sample producer/consumer test in the local stack?
4. Do we need health checks for Kafka and Conduktor readiness?
5. Should the local stack be integrated into the main `docker-compose.yml` via `extends` or kept completely separate?

---

## Next Steps

1. **ARCHITECTURE Phase** (`@code-architect`):
   - Decide Zookeeper vs. KRaft
   - Design network topology and port mapping strategy
   - Create ADR for local dev environment setup

2. **IMPLEMENTATION Phase** (`@code-implement`):
   - Write `docker-compose.local.yml`
   - Configure Conduktor pre-configuration
   - Update README with startup instructions

3. **TEST Phase** (`@test-qa`):
   - Verify Kafka broker connectivity
   - Verify Conduktor UI accessibility
   - Test data persistence across restarts
   - Test environment variable override mechanism

4. **RELEASE Phase** (`@release-pr`):
   - Create PR with `docker-compose.local.yml` and documentation
   - Merge and close GH-5

---

## Appendix: Issue Context

**Original GitHub Issue #5**:
- **Author**: anilogutveren
- **State**: OPEN
- **Labels**: (none)
- **Assignees**: (none)

**Issue Body** (summarized):
- Set up dedicated local dev environment with Kafka broker and Conduktor
- Developers need self-contained environment for local testing
- Separate `docker-compose` file, independent from main application compose
- Services: Zookeeper, Kafka Broker, Conduktor
- Acceptance criteria: All documented above
- Technical notes: Evaluate KRaft, Conduktor licensing, network aliases, named volumes, port mappings

