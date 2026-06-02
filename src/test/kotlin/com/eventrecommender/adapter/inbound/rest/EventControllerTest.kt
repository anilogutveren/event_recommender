package com.eventrecommender.adapter.inbound.rest

import com.eventrecommender.application.port.inbound.CreateEventUseCase
import com.eventrecommender.application.port.inbound.GetEventUseCase
import com.eventrecommender.application.port.inbound.RecommendEventsUseCase
import com.eventrecommender.application.port.outbound.DomainEventPublisher
import com.eventrecommender.application.port.outbound.EventMetricsPort
import com.eventrecommender.application.port.outbound.EventRepository
import com.eventrecommender.domain.model.Category
import com.eventrecommender.domain.model.Event
import com.eventrecommender.domain.model.EventId
import com.eventrecommender.domain.model.Location
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Instant
import java.time.temporal.ChronoUnit

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val objectMapper = ObjectMapper().registerKotlinModule()

    private val berlin = Location(52.52, 13.405, "Berlin", "Germany")

    private fun anEvent(id: EventId = EventId.generate()) = Event(
        id = id,
        title = "Kotlin Conference",
        description = "A conference about Kotlin",
        category = Category.TECHNOLOGY,
        location = berlin,
        venue = "Berlin Expo Center",
        startTime = Instant.now().plus(1, ChronoUnit.DAYS),
        endTime = Instant.now().plus(2, ChronoUnit.DAYS),
        tags = emptySet(),
        createdAt = Instant.now(),
    )

    private fun validCreateRequestBody() = mapOf(
        "title" to "Kotlin Conference",
        "description" to "A conference about Kotlin",
        "category" to "TECHNOLOGY",
        "latitude" to 52.52,
        "longitude" to 13.405,
        "city" to "Berlin",
        "country" to "Germany",
        "venue" to "Berlin Expo Center",
        "startTime" to Instant.now().plus(1, ChronoUnit.DAYS).toString(),
        "endTime" to Instant.now().plus(2, ChronoUnit.DAYS).toString(),
    )

    // AC: GH-3-AC-4, GH-3-AC-8 — TEST-R01: GET by id returns event JSON with 200
    @Test
    fun `GET event by id returns 200 when found`() {
        // Given
        val event = anEvent()
        coEvery { MockUseCaseConfig.getUseCase.execute(any()) } returns event

        // When / Then
        mockMvc.get("/api/v1/events/${event.id.value}")
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value(event.id.value) }
                jsonPath("$.title") { value("Kotlin Conference") }
                jsonPath("$.category") { value("TECHNOLOGY") }
            }
    }

    // AC: GH-3-AC-4, GH-3-AC-8 — TEST-R02: GET by unknown id returns 404
    @Test
    fun `GET event by id returns 404 when not found`() {
        // Given
        coEvery { MockUseCaseConfig.getUseCase.execute(any()) } returns null

        // When / Then
        mockMvc.get("/api/v1/events/nonexistent-id")
            .andExpect { status { isNotFound() } }
    }

    // AC: GH-3-AC-4, GH-3-AC-8 — TEST-R03: POST valid event returns 201 with id
    @Test
    fun `POST create event returns 201`() {
        // Given
        val event = anEvent()
        coEvery { MockUseCaseConfig.createUseCase.execute(any()) } returns event

        // When / Then
        mockMvc.post("/api/v1/events") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(validCreateRequestBody())
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value(event.id.value) }
        }
    }

    // AC: GH-3-AC-8 — TEST-R04: POST with missing required fields returns 400
    @Test
    fun `POST create event returns 400 when title is missing`() {
        // Given — title is required; omitting it causes deserialization failure
        val body = validCreateRequestBody() - "title"

        // When / Then
        mockMvc.post("/api/v1/events") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(body)
        }.andExpect {
            status { isBadRequest() }
        }
    }

    // AC: GH-3-AC-8 — TEST-R05: POST with invalid category enum value returns 400
    @Test
    fun `POST create event returns 400 for invalid category value`() {
        // Given
        val body = validCreateRequestBody() + ("category" to "INVALID_CATEGORY")

        // When / Then
        mockMvc.post("/api/v1/events") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(body)
        }.andExpect {
            status { isBadRequest() }
        }
    }

    // AC: GH-3-AC-4, GH-3-AC-8 — TEST-R06: GET list returns 200 with array
    @Test
    fun `GET list events returns 200`() {
        // Given
        val events = listOf(anEvent(), anEvent())
        coEvery { MockUseCaseConfig.getUseCase.executeAll(any()) } returns events

        // When / Then
        mockMvc.get("/api/v1/events")
            .andExpect {
                status { isOk() }
                jsonPath("$.length()") { value(2) }
            }
    }

    // AC: GH-3-AC-8 — TEST-R07: edge case — empty list returns 200 with empty array
    @Test
    fun `GET list events returns 200 with empty array when no events`() {
        // Given — empty catalogue edge case
        coEvery { MockUseCaseConfig.getUseCase.executeAll(any()) } returns emptyList()

        // When / Then
        mockMvc.get("/api/v1/events")
            .andExpect {
                status { isOk() }
                jsonPath("$.length()") { value(0) }
            }
    }

    // AC: GH-3-AC-8, OWASP-A05 — TEST-R08: error response must not expose stack trace
    @Test
    fun `error response does not expose stack trace in body`() {
        // Given — force a 500 by having the use case throw
        coEvery { MockUseCaseConfig.getUseCase.execute(any()) } throws RuntimeException("Simulated failure")

        // When / Then — OWASP A05: stack traces must never appear in API responses
        mockMvc.get("/api/v1/events/some-id")
            .andExpect {
                status { isInternalServerError() }
                // Must NOT contain stack trace markers
                jsonPath("$.detail") { value("An unexpected error occurred") }
                // Verify no class name / line number leakage
                content { string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("at com."))) }
                content { string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("StackTrace"))) }
            }
    }

    // AC: GH-3-AC-4, GH-3-AC-8 — TEST-R09: POST /recommendations returns 200
    @Test
    fun `POST recommendations returns 200 with ranked event list`() {
        // Given
        val events = listOf(anEvent())
        coEvery { MockUseCaseConfig.recommendUseCase.execute(any()) } returns events

        val body = mapOf("categories" to listOf("TECHNOLOGY"), "limit" to 10)

        // When / Then
        mockMvc.post("/api/v1/events/recommendations") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(body)
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(1) }
            jsonPath("$[0].category") { value("TECHNOLOGY") }
        }
    }

    // AC: GH-3-AC-8 — TEST-R09b: POST /recommendations with empty categories (cold start) returns 200
    @Test
    fun `POST recommendations with empty categories returns 200`() {
        // Given — cold start: no preferences
        coEvery { MockUseCaseConfig.recommendUseCase.execute(any()) } returns emptyList()

        val body = mapOf("categories" to emptyList<String>(), "limit" to 10)

        // When / Then
        mockMvc.post("/api/v1/events/recommendations") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(body)
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(0) }
        }
    }

    /**
     * TestConfiguration that provides mockk stubs for all outbound ports so the
     * full Spring context can be wired without Elasticsearch.
     *
     * Outbound port mocks replace the ES adapters (excluded via application-test.yml).
     * Inbound port companion objects are used for stubbing in individual tests.
     */
    @TestConfiguration
    class MockUseCaseConfig {

        companion object {
            // inbound port mocks - stubbed per test via coEvery
            val createUseCase: CreateEventUseCase = mockk(relaxed = true)
            val getUseCase: GetEventUseCase = mockk(relaxed = true)
            val recommendUseCase: RecommendEventsUseCase = mockk(relaxed = true)

            // outbound port mocks - satisfy ApplicationServiceConfig wiring
            val eventRepository: EventRepository = mockk(relaxed = true)
            val eventMetrics: EventMetricsPort = mockk(relaxed = true)
            val domainEventPublisher: DomainEventPublisher = mockk(relaxed = true)
        }

        @Bean @Primary fun createEventUseCase(): CreateEventUseCase = createUseCase
        @Bean @Primary fun getEventUseCase(): GetEventUseCase = getUseCase
        @Bean @Primary fun recommendEventsUseCase(): RecommendEventsUseCase = recommendUseCase
        @Bean @Primary fun eventRepository(): EventRepository = eventRepository
        @Bean @Primary fun eventMetricsPort(): EventMetricsPort = eventMetrics
        @Bean @Primary fun domainEventPublisher(): DomainEventPublisher = domainEventPublisher
    }
}
