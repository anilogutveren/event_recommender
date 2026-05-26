---
name: test-patterns
description: >
  Test design patterns and techniques for the event_recommender project.
  Load this skill during the TEST phase to select the right technique
  per acceptance criterion and to structure test suites correctly.
---

# Test Design Patterns — event_recommender

Load this skill at the start of Phase 0.5 (Test Design). Select the appropriate technique for each acceptance criterion based on the guidance below.

---

## Test Pyramid

```
        ┌──────────┐
        │  E2E (10%)│  Full recommendation flow, happy path only
        ├──────────┤
        │ Integration│  Spring context + TestContainers (20%)
        │   (20%)   │  Repository + Elasticsearch + external API stubs
        ├──────────┤
        │  Unit (70%)│  Pure logic: scoring, ranking, filters
        └──────────┘
```

**Rule**: If a test can be written as a unit test, do not write it as an integration test.

---

## Technique Selection Guide

| Acceptance Criterion Type | Technique | Example |
|---------------------------|-----------|---------|
| Exact value / formula | **Equivalence Partitioning + Boundary Value** | Score = 0.0 when no matches |
| Multiple conditions combined | **Decision Table** | Genre match × location match × recency |
| Sequential steps / workflows | **State Transition** | Preference setup → recommendation → feedback loop |
| Business rules in plain English | **BDD (Given/When/Then)** | "Given user prefers jazz, when…" |
| Error/edge cases | **Negative Testing** | Empty input, null fields, expired tokens |
| Performance thresholds | **Characterization Test** | Response time ≤ 200ms on mock data |

---

## Technique Details

### Boundary Value Analysis (BVA)
Use for any numeric threshold in the recommender (score cutoff, max results, pagination, date ranges):
```
For maxResults = 20:
  Test: 0, 1, 19, 20, 21, Integer.MAX_VALUE
```

### Equivalence Partitioning
Group inputs into classes where behaviour is identical:
```
User preference match:
  - Exact genre match (score > 0.8)
  - Partial match (0.3–0.8)
  - No match (score = 0.0)
```

### Decision Table
Use when 2+ independent conditions combine to produce a result:

| Genre Match | Location Match | Recency | Expected Bucket |
|-------------|----------------|---------|-----------------|
| Yes | Yes | Recent | High |
| Yes | No | Recent | Medium |
| No | Yes | Any | Low |
| No | No | Any | Excluded |

Each row = one test.

### BDD (Given / When / Then)
Map directly from acceptance criteria to test body:
```kotlin
@Test
fun `should recommend jazz events when user prefers jazz and events are available`() {
    // Given
    val user = UserProfile(preferences = listOf("jazz"))
    val events = listOf(jazzEvent, rockEvent, classicalEvent)
    every { eventRepository.findAll() } returns events

    // When
    val result = recommenderService.recommend(user)

    // Then
    assertThat(result.events).hasSize(1)
    assertThat(result.events.first().id).isEqualTo(jazzEvent.id)
}
```

### State Transition
Use for multi-step flows (onboarding, preference updates, feedback loops):
```
States: NO_HISTORY → HAS_PREFERENCES → HAS_RECOMMENDATIONS → HAS_FEEDBACK
Tests:
  - Transition: NO_HISTORY → HAS_PREFERENCES (submit preferences)
  - Transition: HAS_PREFERENCES → HAS_RECOMMENDATIONS (first recommendation call)
  - Invalid: HAS_FEEDBACK → NO_HISTORY (logout should clear recommendations)
```

---

## Mandatory Edge Cases for event_recommender

Every ticket touching the recommendation engine **must** include tests for:

| Edge Case | Why |
|-----------|-----|
| User with no preference history (cold start) | Most common failure mode for new users |
| Empty event catalogue | System must not crash; return empty list with reason |
| All events in past (expired) | Expired events must be filtered; result may be empty |
| User preference list is empty | Graceful degradation to popularity-based ranking |
| Duplicate events from multiple sources | Deduplication logic must be tested |
| Pagination: page 0, last page, beyond last page | Off-by-one is common in pagination |
| External API returns 429/503 | Circuit breaker / fallback must be tested |

---

## Mockk Patterns (Kotlin)

```kotlin
// Basic mock
val repo = mockk<EventRepository>()
every { repo.findByGenre("jazz") } returns listOf(jazzEvent)

// Verify call with argument capture
val slot = slot<String>()
every { repo.findByGenre(capture(slot)) } returns emptyList()
verify { repo.findByGenre(any()) }
assertThat(slot.captured).isEqualTo("jazz")

// Mock suspend function
coEvery { repo.findRecommended(any()) } returns listOf(event1)
coVerify { repo.findRecommended(userId) }

// Throw on call
every { externalClient.fetchEvents() } throws RuntimeException("API down")

// Relaxed mock (returns defaults, no stubbing needed)
val logger = mockk<Logger>(relaxed = true)
```

---

## TestContainers — Elasticsearch Integration

```kotlin
@SpringBootTest
@Testcontainers
class EventRepositoryIntegrationTest {

    companion object {
        @Container
        @JvmStatic
        val elasticsearch = ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.13.0")
            .withEnv("discovery.type", "single-node")
            .withEnv("xpack.security.enabled", "false")
    }

    // Tests here run against a real Elasticsearch instance
}
```

---

## Test Naming Convention
```
should_<expectedOutcome>_when_<condition>()
```
Examples:
- `should_return_empty_list_when_user_has_no_preferences()`
- `should_throw_NotFoundException_when_event_id_is_unknown()`
- `should_apply_recency_boost_when_event_is_within_7_days()`

---

## Traceability Requirement
Every test **must** map back to an acceptance criterion. Use the comment tag:
```kotlin
// AC: GH-12-AC-3 — User receives no more than 20 recommendations per page
@Test
fun `should limit recommendations to maxResults per page`() { ... }
```

Save the full traceability matrix in `.stage/<TICKET-ID>/testTraceability.md`.
