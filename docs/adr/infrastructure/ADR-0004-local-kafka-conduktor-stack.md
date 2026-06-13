# ADR-0004 — Local Kafka + Conduktor Stack (Integrated into Main Docker Compose)

**Date:** 2026-06-12  
**Status:** Accepted  
**Category:** Infrastructure  
**Ticket:** GH-5  

---

## Context

The `event_recommender` project requires a **local development environment** where developers can:
- Run a Kafka broker locally for event streaming and async processing
- Visually inspect, debug, and replay Kafka topics via a web UI
- Integrate seamlessly with the existing Spring Boot application and Elasticsearch stack

The original plan proposed a separate `docker-compose.local.yml` file to avoid port conflicts. However, **user override directive** specifies: *"Local stack can be integrated in the main stack."* This reframes the decision from **isolation** to **integration with conflict resolution**.

### Key Drivers
1. **Developer experience**: Single `docker compose up` command to boot entire local stack (app + ES + Kafka + UI)
2. **Hexagonal architecture compliance**: Kafka adapter must live in the Infrastructure layer; domain/application layers must not depend on Kafka types
3. **Port conflict resolution**: Main compose already binds port 8080 (app); Conduktor's default is also 8080 → must remap
4. **KRaft vs. Zookeeper**: Kafka 3.3+ supports KRaft (no Zookeeper needed); reduces complexity and container count

---

## Decision

**Integrate Kafka broker and Conduktor services into the main `docker-compose.yml`** (not a separate file), with the following design:

### 1. Kafka Coordination Mode: **KRaft (Kraft Raft)**
- **Why**: Kafka 3.3+ (current standard) supports KRaft natively
- **Benefit**: Eliminates Zookeeper dependency; reduces container count from 3 to 2 (Kafka + Conduktor)
- **Trade-off**: KRaft is production-ready as of Kafka 3.3; no stability concerns for local dev

### 2. Kafka Image: **Confluent Kafka 7.x**
- **Why**: Confluent images are well-maintained, include KRaft support, and integrate cleanly with Spring Boot
- **Alternative considered**: Apache Kafka official image (lighter, but less Spring integration)
- **Alternative considered**: Bitnami Kafka (lightweight, but less ecosystem support)

### 3. Conduktor Edition: **Conduktor Console (Free Tier)**
- **Why**: Free tier is sufficient for local development; no license required
- **Benefit**: Zero-cost, full-featured Kafka UI (topics, messages, consumer groups, schemas)
- **Trade-off**: No ACL/RBAC features (acceptable for local dev)

### 4. Port Mapping Strategy

| Service | Internal Port | Host Port | Rationale |
|---------|---------------|-----------|-----------|
| **Kafka Broker** | 29092 (internal) | 9092 (external) | Standard Kafka port; internal Docker network uses 29092 |
| **Conduktor UI** | 8080 | **8088** | Remapped from 8080 to avoid conflict with Spring Boot app (8080) |
| **Elasticsearch** | 9200 | 9200 | Unchanged (no conflict) |
| **Kibana** | 5601 | 5601 | Unchanged (no conflict) |
| **APM Server** | 8200 | 8200 | Unchanged (no conflict) |
| **OTel Collector** | 4317/4318 | 4317/4318 | Unchanged (no conflict) |

### 5. Network Configuration
- **Docker network**: All services on same `event-recommender` network (or default bridge)
- **Kafka advertised listeners**:
  - **Internal (Docker)**: `kafka:29092` — used by app and other containers
  - **External (Host)**: `localhost:9092` — used by local clients (e.g., CLI tools, external producers)
- **Conduktor**: Configured to connect to `kafka:29092` (internal) on startup

### 6. Volume Strategy
- **Named volume**: `kafka-data` — persists Kafka broker state across restarts
- **Reset procedure**: `docker compose down -v` removes volumes; next `up` starts fresh
- **No Zookeeper volume**: KRaft stores metadata in Kafka itself

### 7. Health Checks
```yaml
# Kafka health check: broker is ready when it responds to metadata requests
healthcheck:
  test: ["CMD", "kafka-broker-api-versions.sh", "--bootstrap-server", "localhost:9092"]
  interval: 10s
  timeout: 5s
  retries: 5

# Conduktor health check: HTTP 200 on /
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8080/"]
  interval: 10s
  timeout: 5s
  retries: 5
```

---

## Alternatives Considered

| Option | Pros | Cons | Verdict |
|--------|------|------|---------|
| **Integrated into main compose** ✅ | Single `docker compose up`; unified network; easier onboarding | Larger compose file; more services to manage | **Selected** — aligns with user override |
| Separate `docker-compose.local.yml` | Isolation; can be toggled on/off | Requires two commands; network complexity; port conflict still possible | Rejected — conflicts with user directive |
| Zookeeper + Kafka (KRaft alternative) | Proven in production | Extra container; more config; KRaft is now standard | Rejected — KRaft is simpler for local dev |
| Bitnami Kafka image | Lightweight; good docs | Less Spring integration; smaller ecosystem | Rejected — Confluent is more mature |
| Conduktor Platform (paid) | Advanced features (ACLs, RBAC) | Cost; overkill for local dev | Rejected — free tier is sufficient |

---

## Hexagonal Architecture Alignment

### Outbound Port Definition (Domain Layer)
The domain layer defines an outbound port for event streaming:

```kotlin
// domain/port/outbound/EventStreamPublisher.kt
interface EventStreamPublisher {
    suspend fun publishEvent(event: DomainEvent): Result<Unit>
}

interface EventStreamSubscriber {
    suspend fun subscribeToTopic(topic: String, handler: suspend (Event) -> Unit)
}
```

**Key constraint**: Domain layer defines the interface; no Kafka types (e.g., `ProducerRecord`, `ConsumerRecord`) leak into domain.

### Driven Adapter (Infrastructure Layer)
The Kafka adapter implements the outbound port:

```kotlin
// adapter/outbound/messaging/KafkaEventPublisher.kt
@Component
class KafkaEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>
) : EventStreamPublisher {
    override suspend fun publishEvent(event: DomainEvent): Result<Unit> {
        // Kafka-specific logic here
        // Converts DomainEvent → JSON → ProducerRecord
        // Returns Result<Unit> (no Kafka types exposed)
    }
}
```

**Constraint enforcement**:
- Application services depend on `EventStreamPublisher` (port interface), not `KafkaEventPublisher` (adapter)
- Kafka dependencies (`spring-kafka`, `kafka-clients`) are **only** in the `adapter` module
- Domain and Application modules have **zero** Kafka imports

### Layer Mapping
| Hexagonal Layer | Component | Kafka Dependency |
|-----------------|-----------|------------------|
| **Domain** | `DomainEvent`, `EventStreamPublisher` (interface) | ❌ None |
| **Application** | Use case services that call `EventStreamPublisher` | ❌ None (depends on interface only) |
| **Infrastructure** | `KafkaEventPublisher`, `KafkaEventSubscriber` (implementations) | ✅ Spring Kafka, Kafka Clients |
| **Interface** | REST controllers, event listeners | ❌ None (delegates to Application) |

---

## Consequences

### Positive
- **Single command startup**: `docker compose up` boots app + ES + Kafka + Conduktor
- **Unified network**: All services communicate via Docker DNS; no manual host mapping
- **Reduced complexity**: KRaft eliminates Zookeeper; fewer containers, simpler config
- **Developer experience**: Conduktor UI at `http://localhost:8088` (easy to remember; no conflict)
- **Hexagonal compliance**: Kafka adapter is properly isolated in Infrastructure layer
- **Persistence**: Named volumes ensure Kafka data survives restarts
- **Scalability ready**: When moving to production, Kafka cluster can be externalized without code changes (only port/config changes)

### Negative / Trade-offs
- **Larger compose file**: Main `docker-compose.yml` now includes messaging services (acceptable; documented)
- **Port remapping**: Conduktor on 8088 instead of 8080 (minor; documented in README)
- **KRaft maturity**: KRaft is stable in Kafka 3.3+, but less battle-tested than Zookeeper in production (mitigated: local dev only)
- **Confluent image size**: Confluent images are larger than Apache official (trade-off for integration)

### Neutral
- **No breaking changes**: Existing services (app, ES, Kibana, APM, OTel) remain unchanged
- **Backward compatible**: Developers can still run `docker compose up` without Kafka if they don't need it (by selectively starting services)

---

## Implementation Notes

### docker-compose.yml Changes
1. Add `kafka` service with KRaft configuration
2. Add `conduktor` service pointing to `kafka:29092`
3. Add `kafka-data` named volume
4. Update app service `depends_on` to include Kafka (if async event publishing is required)
5. Document port mappings in inline comments

### Environment Variables
- `KAFKA_BROKER_ADDRESS`: Default `kafka:29092` (internal); can be overridden for external clients
- `CONDUKTOR_KAFKA_BOOTSTRAP_SERVERS`: Default `kafka:29092`

### Documentation (README.md)
```markdown
## Local Development Stack

Start the full local stack (app + Elasticsearch + Kafka + Conduktor):

\`\`\`bash
docker compose up
\`\`\`

### Service Endpoints
- **Spring Boot API**: http://localhost:8080
- **Elasticsearch**: http://localhost:9200
- **Kibana**: http://localhost:5601
- **Kafka Broker**: localhost:9092 (external), kafka:29092 (internal)
- **Conduktor UI**: http://localhost:8088
- **APM Server**: http://localhost:8200
- **OTel Collector**: localhost:4317 (gRPC), localhost:4318 (HTTP)

### Reset Kafka Data
\`\`\`bash
docker compose down -v  # Remove named volumes
docker compose up       # Start fresh
\`\`\`
```

---

## References

- **Plan**: `.stage/GH-5/plan.md`
- **Architecture**: [ADR-0001: Hexagonal Architecture](../architecture/ADR-0001-hexagonal-architecture.md)
- **Tech Stack**: [ADR-0002: Kotlin + Spring Boot](../technology/ADR-0002-kotlin-spring-boot.md)
- **Monitoring**: [ADR-0003: Elasticsearch Monitoring-First](../technology/ADR-0003-elasticsearch-monitoring-first.md)
- **Kafka Docs**: https://kafka.apache.org/documentation/#kraft
- **Confluent Docker**: https://hub.docker.com/r/confluentinc/cp-kafka
- **Conduktor**: https://www.conduktor.io/

---

## Related ADRs

- **ADR-0001**: Hexagonal Architecture (defines port/adapter pattern)
- **ADR-0002**: Kotlin + Spring Boot (defines framework)
- **ADR-0003**: Elasticsearch (defines data store; Kafka is complementary for async)

---

## Approval

- **Reviewed by**: Architecture Governance (`@code-architect`)
- **Approved by**: [Pending user review]
- **Date approved**: [TBD]
