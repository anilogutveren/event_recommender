---
id: ADR-0001
title: Hexagonal Architecture as Base Pattern
category: architecture
status: accepted
created: 2026-05-28
author: Team Event Recommender
superseded_by: null
related_reqs: []
---

# ADR-0001: Hexagonal Architecture as Base Pattern

## Context

The event_recommender project is being built as a greenfield project. We need an architecture pattern that:

- Provides clear separation between business logic and infrastructure
- Enables easy testing of business logic without external dependencies
- Allows flexible replacement of infrastructure components (e.g. database, message broker)
- Supports Clean Architecture / Onion principles with testable, isolated domain logic

## Decision

We use **Hexagonal Architecture** (Ports & Adapters), also known as Clean/Onion Architecture, as the foundational architecture pattern for the event_recommender service.

### Package Structure

```
com.eventrecommender/
├── domain/
│   ├── model/           # Entities, Value Objects, Aggregates
│   ├── service/         # Domain Services
│   └── event/           # Domain Events
├── application/
│   ├── port/
│   │   ├── inbound/     # Use Case Interfaces (Inbound Ports)
│   │   └── outbound/    # Repository/Gateway Interfaces (Outbound Ports)
│   └── service/         # Use Case Implementations (Application Services)
└── adapter/
    ├── inbound/
    │   ├── rest/         # REST Controllers (Driving Adapters)
    │   └── event/        # Event Listeners (Driving Adapters)
    └── outbound/
        ├── persistence/  # Elasticsearch Repositories (Driven Adapters)
        ├── messaging/    # Message Producers (Driven Adapters)
        └── client/       # External API Clients (Driven Adapters)
```

## Consequences

### Positive

- Business logic is free of framework dependencies → easily testable
- Infrastructure components are replaceable (e.g. Elasticsearch → another store)
- Clear boundaries promote parallel development
- Domain logic is testable in isolation

### Negative

- More boilerplate due to port interfaces and adapter classes
- Learning curve for those unfamiliar with Hexagonal Architecture
- Mapping between domain models and persistence models required

### Neutral

- Spring Boot remains the framework of choice, but is only used in adapters
- Kotlin Coroutines for reactive adapters, suspending functions on ports

## References

- [Architecture Overview](../overview.md)

