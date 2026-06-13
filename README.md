# event_recommender

An AI-powered event recommendation system — built step by step using custom Copilot agents.

## Structure

```
event_recommender/
├── .github/
│   └── agents/       ← shared team agents (committed)
├── .vscode/
│   └── agents/       ← personal agents (gitignored)
└── README.md
```

## How to build this project

Each feature is implemented by invoking the relevant agent in VS Code Copilot Chat (Agent mode).
Agents live in `.github/agents/` — open Copilot Chat, select the agent, and follow its session flow.

---

## Local Development Stack

The project includes a complete local development environment with all required services integrated into a single Docker Compose stack.

### Quick Start

Start the entire local stack (Spring Boot app, Elasticsearch, Kafka, Conduktor, APM, and OpenTelemetry):

```bash
docker compose up
```

This command boots all services with health checks and automatic service discovery. Services will be ready within 30–60 seconds.

### Service Endpoints

| Service | URL / Port | Purpose |
|---------|-----------|---------|
| **Spring Boot API** | http://localhost:8080 | REST API server |
| **Elasticsearch** | http://localhost:9200 | Search and data store |
| **Kibana** | http://localhost:5601 | Elasticsearch UI and monitoring |
| **Kafka Broker** | localhost:9092 (external), kafka:29092 (internal) | Event streaming |
| **Conduktor** | http://localhost:8088 | Kafka topic inspection and debugging |
| **APM Server** | http://localhost:8200 | Application Performance Monitoring |
| **OTel Collector** | localhost:4317 (gRPC), localhost:4318 (HTTP) | OpenTelemetry trace collection |

### Kafka Configuration

- **Mode**: KRaft (no Zookeeper dependency)
- **Image**: Confluent Kafka 7.6.1
- **Internal DNS**: `kafka:29092` (used by app and containers)
- **External Access**: `localhost:9092` (used by CLI tools and external clients)
- **Data Persistence**: Named volume `kafka-data` — survives container restarts

### Conduktor (Kafka UI)

Conduktor is pre-configured to connect to the local Kafka broker on startup. Simply navigate to **http://localhost:8088** to:
- Inspect topics and messages
- Monitor consumer groups
- Debug event streaming issues
- View schema registry (if configured)

**Admin credentials** (local dev only):
- Email: `admin@conduktor.io`
- Password: `admin`

### Reset Kafka Data

To clear all Kafka data and start fresh:

```bash
docker compose down -v  # Remove all named volumes
docker compose up       # Start fresh
```

### Connecting External Clients

To connect external CLI tools (e.g., `kcat`, `kafka-console-producer`) to the local Kafka broker:

```bash
# Example: List topics
kcat -b localhost:9092 -L

# Example: Produce a message
echo "test message" | kcat -b localhost:9092 -t my-topic -P

# Example: Consume messages
kcat -b localhost:9092 -t my-topic -C
```

### Troubleshooting

**Kafka broker not ready?**
```bash
# Check Kafka health
docker compose logs kafka

# Wait for health check to pass (watch for "Broker is ready")
docker compose ps
```

**Conduktor not connecting to Kafka?**
```bash
# Check Conduktor logs
docker compose logs conduktor

# Verify Kafka is healthy
docker compose logs kafka | grep "started"
```

**Port already in use?**
```bash
# Find what's using the port (e.g., 8080)
lsof -i :8080

# If needed, stop the conflicting service and retry
docker compose down
docker compose up
```

---

## Architecture & Design Decisions

- **Hexagonal Architecture**: Domain logic is isolated from infrastructure (Kafka, Elasticsearch, etc.)
- **Kafka Integration**: Event streaming via Apache Kafka (KRaft mode); see [ADR-0004](docs/adr/infrastructure/ADR-0004-local-kafka-conduktor-stack.md)
- **Monitoring-First**: Elasticsearch and OpenTelemetry are set up from day one
- **Security**: All endpoints require authentication except `/actuator/health` and `/actuator/prometheus`

See [`.stage/docs/architecture.md`](.stage/docs/architecture.md) for the complete system architecture and [ADRs](docs/adr/) for design decisions.

---

## Build & Run

### Prerequisites

- **JDK 21** for local Gradle builds. Gradle 8.14 does not yet support JDK 25; if your host
  ships a newer JDK, either install JDK 21 (e.g. `brew install openjdk@21` or via
  [SDKMAN](https://sdkman.io/)) and point `JAVA_HOME` at it, or skip the host build entirely
  and use the Docker workflow below — `docker build` and `docker compose up` work end-to-end
  without a host JDK.
- **Docker Desktop** (or any Docker engine) with Compose v2 for the local stack.

### Build the project (host)

```bash
./gradlew build          # Compile + test
```

### Build & start the dev server (Docker — no host JDK required)

```bash
docker compose up --build   # Build the image and start all services
```

The Spring Boot app will be available at **http://localhost:8080**. The image is now a
self-contained multi-stage build, so the host does not need Gradle or a JDK.

### Run tests

```bash
./gradlew test           # Run test suite
```

---

## Documentation

- [System Architecture](.stage/docs/architecture.md) — Component diagram, tech stack, module structure
- [ADR-0001: Hexagonal Architecture](docs/adr/architecture/ADR-0001-hexagonal-architecture.md)
- [ADR-0004: Local Kafka + Conduktor Stack](docs/adr/infrastructure/ADR-0004-local-kafka-conduktor-stack.md)
- [SDLC Agents](AGENTS.md) — How to invoke agents for features, bugs, and hotfixes
