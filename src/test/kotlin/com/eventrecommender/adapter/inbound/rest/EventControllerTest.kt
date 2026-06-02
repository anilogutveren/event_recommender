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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
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

    @Test
    fun `GET event by id returns 200 when found`() {
        val event = anEvent()
        coEvery { MockUseCaseConfig.getUseCase.execute(any()) } returns event

        mockMvc.get("/api/v1/events/${event.id.value}")
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value(event.id.value) }
                jsonPath("$.title") { value("Kotlin Conference") }
                jsonPath("$.category") { value("TECHNOLOGY") }
            }
    }

    @Test
    fun `GET event by id returns 404 when not found`() {
        coEvery { MockUseCaseConfig.getUseCase.execute(any()) } returns null

        mockMvc.get("/api/v1/events/nonexistent-id")
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `POST create event returns 201`() {
        val event = anEvent()
        coEvery { MockUseCaseConfig.createUseCase.execute(any()) } returns event

        val requestBody = mapOf(
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

        mockMvc.post("/api/v1/events") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(requestBody)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value(event.id.value) }
        }
    }

    @Test
    fun `GET list events returns 200`() {
        val events = listOf(anEvent(), anEvent())
        coEvery { MockUseCaseConfig.getUseCase.executeAll(any()) } returns events

        mockMvc.get("/api/v1/events")
            .andExpect {
                status { isOk() }
                jsonPath("$.length()") { value(2) }
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
