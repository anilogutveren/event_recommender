package com.eventrecommender.application.service

import com.eventrecommender.application.port.inbound.CreateEventCommand
import com.eventrecommender.application.port.inbound.FindEventQuery
import com.eventrecommender.application.port.inbound.ListEventsQuery
import com.eventrecommender.application.port.inbound.RecommendEventsQuery
import com.eventrecommender.application.port.outbound.DomainEventPublisher
import com.eventrecommender.application.port.outbound.EventMetricsPort
import com.eventrecommender.application.port.outbound.EventRepository
import com.eventrecommender.domain.model.Category
import com.eventrecommender.domain.model.Event
import com.eventrecommender.domain.model.EventId
import com.eventrecommender.domain.model.Location
import com.eventrecommender.domain.service.EventDomainService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class EventApplicationServiceTest {

    private val eventRepository: EventRepository = mockk()
    private val eventMetrics: EventMetricsPort = mockk(relaxed = true)
    private val domainEventPublisher: DomainEventPublisher = mockk(relaxed = true)
    private val domainService = EventDomainService()

    private val service = EventApplicationService(
        eventRepository = eventRepository,
        eventMetrics = eventMetrics,
        eventPublisher = domainEventPublisher,
        domainService = domainService,
    )

    private val berlin = Location(52.52, 13.405, "Berlin", "Germany")

    private fun aCreateCommand() = CreateEventCommand(
        title = "Kotlin Conference",
        description = "A conference about Kotlin",
        category = Category.TECHNOLOGY,
        location = berlin,
        venue = "Berlin Expo Center",
        startTime = Instant.now().plus(1, ChronoUnit.DAYS),
        endTime = Instant.now().plus(2, ChronoUnit.DAYS),
    )

    private fun anEvent(id: EventId = EventId.generate()): Event = Event(
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
    fun `createEvent saves event and publishes domain event`() = runTest {
        val savedEvent = anEvent()
        coEvery { eventRepository.save(any()) } returns savedEvent
        every { domainEventPublisher.publish(any()) } just runs

        val result = service.execute(aCreateCommand())

        assertEquals(savedEvent.id, result.id)
        coVerify(exactly = 1) { eventRepository.save(any()) }
        coVerify { domainEventPublisher.publish(any()) }
    }

    @Test
    fun `findEvent returns event when found`() = runTest {
        val event = anEvent()
        coEvery { eventRepository.findById(event.id) } returns event

        val result = service.execute(FindEventQuery(event.id))

        assertEquals(event, result)
    }

    @Test
    fun `findEvent returns null when not found`() = runTest {
        val id = EventId.generate()
        coEvery { eventRepository.findById(id) } returns null

        val result = service.execute(FindEventQuery(id))

        assertNull(result)
    }

    @Test
    fun `listEvents delegates to repository`() = runTest {
        val events = listOf(anEvent(), anEvent())
        coEvery { eventRepository.findAll(0, 20) } returns events

        val result = service.executeAll(ListEventsQuery(0, 20))

        assertEquals(2, result.size)
    }

    @Test
    fun `recommendEvents filters and ranks events`() = runTest {
        val techEvent = anEvent()
        coEvery {
            eventRepository.findByCategories(setOf(Category.TECHNOLOGY), 30)
        } returns listOf(techEvent)

        val result = service.execute(
            RecommendEventsQuery(
                preferredCategories = setOf(Category.TECHNOLOGY),
                userLocation = berlin,
                maxDistanceKm = 100.0,
                limit = 10,
            ),
        )

        assertEquals(1, result.size)
        assertEquals(techEvent.id, result.first().id)
    }
}
