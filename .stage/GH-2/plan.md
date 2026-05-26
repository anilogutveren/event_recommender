# GH-2 — Story Brief

## Ticket Context
- **Ticket ID**: GH-2
- **Title**: Migrate event_recommender from Spring Boot 3.x to Spring Boot 4.x
- **Type**: Enhancement / Migration
- **Priority**: High
- **GitHub Issue**: https://github.com/anilogutveren/event_recommender/issues/2

## Problem Statement
The `event_recommender` application is built on Spring Boot 3.4.1, which will eventually reach end-of-life.
Spring Boot 4.x (expected to align with Spring Framework 7) brings long-term support, improved virtual-thread integration,
Jakarta namespace consolidation, and performance improvements. Staying on an aging major version creates mounting
technical debt: deprecated APIs accumulate, plugin compatibility degrades, and CVE patches lag behind. This migration
ensures the project remains on a supported, modern baseline with access to the latest Kotlin and Spring ecosystem features.

## User Story
As a backend engineer, I want the application to run on Spring Boot 4.x, so that the project benefits from
long-term support, the latest Spring features, and a clean, non-deprecated API surface.

## Acceptance Criteria
- [ ] Given the Gradle build file, when `./gradlew build` is run, then the build completes successfully with Spring Boot 4.x declared
- [ ] Given the Spring Boot 4 migration, when the application starts, then no `DeprecationWarning` or removed-API errors appear in logs
- [ ] Given the Kotlin version compatibility requirement, when the build runs, then the Kotlin version is aligned with Spring Boot 4.x requirements
- [ ] Given the updated `application.yml`, when the application starts, then all configuration properties are valid and no `UnknownPropertyException` is thrown
- [ ] Given the full test suite, when `./gradlew test` runs, then all tests pass with zero failures

## Scope
### In scope
- Update `spring-boot` plugin version and `spring-boot-starter-*` dependencies to 4.x in `build.gradle.kts`
- Update `io.spring.dependency-management` plugin if required
- Update Kotlin version if Spring Boot 4.x mandates a newer Kotlin
- Update `kotlin("plugin.spring")` version to match
- Resolve deprecated APIs surfaced by the Spring Boot 4 migration
- Validate `application.yml` for renamed/removed properties
- Update Jackson, Micrometer, OpenTelemetry, and other managed dependencies that may conflict
- Ensure `./gradlew test` passes green
- Update `README.md` with new runtime requirements

### Out of scope
- Adding new application features beyond what is needed for migration compatibility
- Changing the overall application architecture or introducing new modules
- Migrating to a different build tool (stays on Gradle Kotlin DSL)
- Upgrading Java toolchain beyond 21 (unless Spring Boot 4.x strictly requires it)

## Technical Notes
- **Current baseline**: Spring Boot `3.4.1`, Kotlin `2.1.0`, JVM 21, Gradle 8.x
- **Target**: Spring Boot `4.0.x` (latest stable), Kotlin aligned with Spring Boot 4.x requirements
- **Key migration risks**:
  - Jakarta namespace: already handled in SB 3.x (`jakarta.*` imports) — should be clean
  - Spring Security: configuration DSL changed in SB 3; SB 4 may have further changes (N/A if not yet used)
  - Spring Data: API changes for repository query methods
  - `spring-boot-starter-data-elasticsearch` removed in GH-1 — reduces migration surface
  - OTel / Micrometer: version alignment required with new BOM
  - `io.spring.dependency-management` plugin may be deprecated/removed (Spring Boot 4 ships its own catalog)
- **Dependencies currently in use**: `spring-boot-starter-web`, `spring-boot-starter-actuator`, `micrometer-registry-prometheus`, `micrometer-tracing-bridge-otel`, `opentelemetry-spring-boot-starter:2.10.0`, `jackson-module-kotlin`, `kotlin-reflect`, `spring-boot-starter-test`
- **Performance targets**: Build time within 2× of current; no regression in startup time
- **Security constraints**: No new endpoints; CVE-free dependency versions

## Open Questions
- Q1: What is the latest stable Spring Boot 4.x version available at migration time? (confirm before implementation)
- Q2: Does Spring Boot 4.x require Kotlin 2.2+? (`-Xannotation-default-target` was a Kotlin 2.2 flag — would then be available)
- Q3: Is `io.spring.dependency-management` plugin still needed with Spring Boot 4.x, or does the new version ship a Gradle version catalog instead?
- Q4: Does `opentelemetry-spring-boot-starter` have a SB4-compatible release by the time of migration?
