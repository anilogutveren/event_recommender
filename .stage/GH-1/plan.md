# GH-1 — Story Brief

## Ticket Context
- **Ticket ID**: GH-1
- **Title**: Remove Elasticsearch integration (defer to later milestone)
- **Type**: Refactor / Chore
- **Priority**: High (blocks clean local dev and CI)
- **GitHub Issue**: https://github.com/anilogutveren/event_recommender/issues/1

## Problem Statement
The initial scaffold introduced Elasticsearch as a dependency before the domain model
exists. This forces every developer and CI runner to have a live ES instance available
even though no domain logic uses it yet. Removing ES now keeps the project bootable
with zero external infrastructure until the recommendation engine domain is ready.

## User Story
As a developer on event_recommender, I want the app to start and build without a
running Elasticsearch instance, so that I can iterate on the domain model without
infrastructure overhead.

## Acceptance Criteria
- [ ] Given `build.gradle.kts` is the dependency manifest, when `spring-boot-starter-data-elasticsearch` is removed, then `./gradlew build` completes without errors
- [ ] Given `ElasticsearchHealthIndicator.kt` exists, when it is deleted, then the Actuator `/actuator/health` endpoint still responds (without an `elasticsearch` component)
- [ ] Given `ElasticsearchMetrics.kt` exists, when it is deleted, then no `es.*` metrics appear at `/actuator/prometheus`
- [ ] Given `ElasticsearchConfig.kt` exists, when it is deleted, then no ES-related beans are registered in the Spring context
- [ ] Given `ElasticsearchMonitoringIntegrationTest.kt` exists, when it is deleted, then `./gradlew test` passes without requiring a Testcontainers ES instance
- [ ] Given `testcontainers:elasticsearch` and `testcontainers:junit-jupiter` are in `build.gradle.kts`, when they are removed, then the test classpath has no Testcontainers dependency
- [ ] Given `docker-compose.yml` defines an `elasticsearch` service, when that service and its `depends_on` reference are removed, then `docker-compose up` starts only the `app` service
- [ ] Given `application.yml` contains `spring.elasticsearch` and `management.health.elasticsearch`, when those blocks are removed, then the app starts with no ES-related warnings in the log
- [ ] Given all changes above are applied, when `./gradlew bootRun` is executed, then the app starts on port 8080 with no errors

## Scope
### In scope
- Delete: `ElasticsearchHealthIndicator.kt`, `ElasticsearchMetrics.kt`, `ElasticsearchConfig.kt`
- Delete: `ElasticsearchMonitoringIntegrationTest.kt`
- Edit: `build.gradle.kts` — remove ES + Testcontainers dependencies
- Edit: `application.yml` — remove `spring.elasticsearch` block and `management.health.elasticsearch`
- Edit: `docker-compose.yml` — remove `elasticsearch` service and `app.depends_on`
- Edit: `EventRecommenderApplication.kt` — remove `@EnableScheduling` if no scheduled tasks remain

### Out of scope
- Replacing Elasticsearch with another data store
- Adding new monitoring targets or health indicators
- Any feature development
- Removing Micrometer, Prometheus, or Actuator (those stay)

## Technical Notes
- **Data sources**: N/A — pure cleanup
- **Dependencies to remove**:
  - `org.springframework.boot:spring-boot-starter-data-elasticsearch`
  - `org.testcontainers:elasticsearch:1.20.4`
  - `org.testcontainers:junit-jupiter:1.20.4`
- **Dependencies to keep**: Actuator, Micrometer, Prometheus, OTel, Jackson, Kotlin reflect
- **Performance targets**: `./gradlew build -x test` must complete; `./gradlew bootRun` must start cleanly
- **Security constraints**: None for this ticket

## Open Questions
- Should `@EnableScheduling` on `EventRecommenderApplication` be removed too? (It was added only for the ES doc-count gauge `@Scheduled` method — yes, remove it with the metrics class)
- Should `docker-compose.yml` keep the `app` service at all, or simplify to an empty file? (Keep the `app` service skeleton for future use)
