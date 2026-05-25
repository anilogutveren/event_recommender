# event_recommender — System Architecture

**Date:** 2026-05-25
**Owner:** @code-architect
**Status:** Active

---

## System Overview

`event_recommender` is a Kotlin/Spring Boot REST API that recommends events (concerts, conferences, meetups, sports) to users based on their preference profiles and attendance history. Elasticsearch serves as the primary search and data store, chosen for its full-text search, geo-filtering, and native vector similarity capabilities needed for semantic recommendation.

---

## Component Diagram

```mermaid
graph TB
    subgraph Client
        WEB[Web / Mobile Client]
    end

    subgraph API Layer
        GW[Spring Boot REST API<br/>Kotlin + Spring Web]
        ACT[Spring Actuator<br/>Health & Metrics]
    end

    subgraph Recommendation Engine
        REC[Recommender Service<br/>Scoring & Ranking]
        PREF[User Preference Resolver]
    end

    subgraph Data Layer
        ES[(Elasticsearch 8.x<br/>Events + User Profiles)]
        CACHE[Redis Cache<br/>Hot recommendations]
    end

    subgraph Observability
        PROM[Micrometer / Prometheus<br/>Metrics]
        OT[OpenTelemetry<br/>Traces]
    end

    subgraph External
        TM[Ticketmaster API]
        EB[Eventbrite API]
    end

    WEB --> GW
    GW --> REC
    REC --> PREF
    PREF --> ES
    REC --> ES
    REC --> CACHE
    GW --> ACT
    ACT --> PROM
    GW -.-> OT
    REC -.-> OT
    ES -.-> PROM
    GW --> TM
    GW --> EB
```

---

## Tech Stack

| Concern | Technology | ADR |
|---------|-----------|-----|
| Language | Kotlin 2.x | ADR-0001 |
| Runtime | JVM 21 | ADR-0001 |
| Framework | Spring Boot 3.x | ADR-0001 |
| Build | Gradle (Kotlin DSL) | ADR-0001 |
| Search / Data | Elasticsearch 8.x | ADR-0002 |
| Monitoring | Micrometer + Prometheus + Actuator | ADR-0002 |
| Tracing | OpenTelemetry (OTLP) | ADR-0002 |
| Cache | Redis (future — not yet implemented) | TBD |
| Auth | JWT / Spring Security (future) | TBD |

---

## Module Structure

```
src/
  main/kotlin/com/eventrecommender/
    EventRecommenderApplication.kt   ← Spring Boot entry point
    api/                             ← HTTP route handlers + request/response schemas
    recommender/                     ← Recommendation algorithms and scoring
    data/                            ← Elasticsearch repositories + domain models
    integrations/                    ← External API clients (Ticketmaster, Eventbrite)
    workers/                         ← Background jobs (pre-compute recommendations)
    config/                          ← Spring configuration classes
    monitoring/                      ← Custom Micrometer metrics + health indicators
  main/resources/
    application.yml
  test/kotlin/com/eventrecommender/
    ...
```

---

## Key Constraints

- **Monitoring-first**: Elasticsearch monitoring is set up before domain logic — all ES operations emit metrics from day one
- **Immutability**: data classes, `val` by default, no mutation of shared state
- **No persistence layer before search layer**: Elasticsearch is the primary store; no relational DB initially
- **Security**: all endpoints require auth except `/actuator/health` and `/actuator/prometheus`
- **Performance target**: recommendation response <200ms at p95

---

## Build & Run

```bash
./gradlew build          # compile + test
./gradlew bootRun        # start dev server (port 8080)
./gradlew test           # run test suite
```
