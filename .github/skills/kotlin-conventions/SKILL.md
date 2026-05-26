---
name: kotlin-conventions
description: >
  Kotlin and Spring Boot coding conventions for the event_recommender project.
  Load this skill when reviewing or writing Kotlin code to ensure consistency
  with the project's style, patterns, and framework usage.
---

# Kotlin + Spring Boot Conventions — event_recommender

These conventions are **non-negotiable** and enforced during code review. Reference them during Phase 1 (Codebase Review) and Phase 2 (Implementation).

---

## Language Conventions

### Data Modelling
- Use `data class` for all DTOs, request/response bodies, and value objects
- Use `sealed class` (or `sealed interface`) for domain operation results:
  ```kotlin
  sealed class RecommendationResult {
      data class Success(val events: List<Event>) : RecommendationResult()
      data class Empty(val reason: String) : RecommendationResult()
      data class Error(val cause: Throwable) : RecommendationResult()
  }
  ```
- Prefer `value class` (inline classes) for typed IDs to prevent primitive obsession:
  ```kotlin
  @JvmInline value class UserId(val value: String)
  @JvmInline value class EventId(val value: String)
  ```

### Null Safety
- Never use `!!` (not-null assertion) in production code — use `?: throw`, `let`, or `?.`
- Prefer `?.let { }` for null-conditional logic; avoid nested null checks
- Clearly distinguish nullable (`String?`) from non-null (`String`) in function signatures

### Functions
- Functions ≤ 50 lines; files ≤ 400 lines
- Max 4 levels of nesting — use guard clauses and early returns
- Extension functions preferred over utility classes:
  ```kotlin
  // Prefer this
  fun List<Event>.scoreFor(user: UserProfile): List<ScoredEvent>
  // Over this
  object EventScorer { fun score(events: List<Event>, user: UserProfile) }
  ```
- Use `companion object` for factory methods, constants, and named constructors

### Immutability
- Prefer `val` over `var` — `var` must be justified
- Return new collections; never mutate a passed-in list or map
- Use `copy()` on data classes instead of mutating fields

### Error Handling
- Use `sealed class` results or `kotlin.Result<T>` for expected error conditions — not exceptions
- Throw exceptions only for **programming errors** or truly exceptional situations
- Never swallow exceptions with empty `catch` blocks

---

## Coroutines & Async
- All I/O operations (DB queries, external API calls) must be `suspend` functions
- Use `Flow<T>` for streaming or paginated data sources
- Use `coroutineScope { }` for structured concurrency — never `GlobalScope`
- Dispatchers: `Dispatchers.IO` for blocking I/O, `Dispatchers.Default` for CPU work
- Avoid `runBlocking` in production code (acceptable in tests)

---

## Spring Boot Patterns

### Dependency Injection
- Constructor injection only — no `@Autowired` on fields or setters
- Mark all Spring beans with the minimal necessary annotation (`@Service`, `@Repository`, `@Component`)
- Use `@ConfigurationProperties` for typed config binding:
  ```kotlin
  @ConfigurationProperties(prefix = "recommender")
  data class RecommenderProperties(val maxResults: Int = 20, val scoringVersion: String = "v1")
  ```

### Layer Boundaries
| Layer | Annotation | Responsibility |
|-------|-----------|----------------|
| `api/` | `@RestController` | HTTP in/out, validation, auth |
| `recommender/` | `@Service` | Business logic only |
| `data/` | `@Repository` | Data access only |
| `integrations/` | `@Component` | External API clients |

- Controllers never contain business logic — delegate to a `@Service`
- Services never import Spring MVC types (`HttpServletRequest`, `ResponseEntity`)
- Repositories return domain objects, not raw DB rows

### Validation
- Use `@Valid` + Jakarta Bean Validation annotations on request DTOs
- Custom validators in `api/validation/`
- Validation only at the API boundary — never inside service or domain logic

---

## Testing Stack
- **Framework**: JUnit 5 (`@Test`, `@ParameterizedTest`)
- **Mocking**: Mockk (`mockk<T>()`, `every { }`, `verify { }`) — not Mockito
- **Assertions**: AssertJ (`assertThat(result).isEqualTo(...)`)
- **Integration**: `@SpringBootTest` + TestContainers for Elasticsearch
- **Test naming**: `should_<expectedBehaviour>_when_<condition>()`
  ```kotlin
  @Test
  fun `should return empty list when user has no event history`() { ... }
  ```
- Test files mirror source structure: `src/test/kotlin/com/eventrecommender/<layer>/`

---

## Naming Conventions
| Type | Convention | Example |
|------|-----------|---------|
| Class | PascalCase + role suffix | `EventRecommenderService` |
| Function | camelCase, verb-first | `findRecommendedEvents()` |
| Property/Val | camelCase | `maxRecommendations` |
| Constant | SCREAMING_SNAKE | `MAX_RESULT_SIZE` |
| Package | lowercase, singular | `com.eventrecommender.recommender` |
| Test class | `<Subject>Test` | `EventScorerTest` |

---

## Build & Tooling
- Build: Gradle with Kotlin DSL (`build.gradle.kts`)
- Formatter: ktlint (enforced in CI)
- Run all checks: `./gradlew check`
- Run tests only: `./gradlew test`
- Build fat jar: `./gradlew bootJar`
