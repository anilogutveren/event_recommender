package com.eventrecommender.application.service

import com.eventrecommender.application.port.inbound.CreateEventCommand
import com.eventrecommender.application.port.inbound.FindEventQuery
import com.eventrecommender.application.port.inbound.ListEventsQuery
import com.eventrecommender.application.port.inbound.RecommendEventsQuery
import com.eventrecommender.application.port.outbound.DomainEventPublisher
import com.eventrecommender.application.port.outbound.EventMetricsPort
import com.eventrecommender.application.port.outbound.EventRepository
import com.eventrecommender.domain.event.EventCreatedEvent
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
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
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

    // AC: GH-3-AC-7 — TEST-A01: create saves event and publishes domain event
    @Test
    fun `createEvent saves event and publishes domain event`() = runTest {
        // Given
        val savedEvent = anEvent()
        coEvery { eventRepository.save(any()) } returns savedEvent
        every { domainEventPublisher.publish(any()) } just runs

        // When
        val result = service.execute(aCreateCommand())

        // Then
        assertEquals(savedEvent.id, result.id)
        coVerify(exactly = 1) { eventRepository.save(any()) }
        coVerify { domainEventPublisher.publish(any()) }
    }

    // AC: GH-3-AC-6 — TEST-A07: createEvent records metrics (recordQueryDuration + incrementIndexOperation)
    @Test
    fun `createEvent records query duration and index operation metrics`() = runTest {
        // Given
        val savedEvent = anEvent()
        coEvery { eventRepository.save(any()) } returns savedEvent

        // When
        service.execute(aCreateCommand())

        // Then — AC-6: Micrometer metrics called on every index operation
        verify(exactly = 1) { eventMetrics.incrementIndexOperation() }
        verify(atLeast = 1) { eventMetrics.recordQueryDuration(any()) }
    }

    // AC: GH-3-AC-7 — TEST-A01: domain event published is an EventCreatedEvent
    @Test
    fun `createEvent publishes EventCreatedEvent with correct id`() = runTest {
        // Given
        val savedEvent = anEvent()
        coEvery { eventRepository.save(any()) } returns savedEvent
        val capturedEvents = mutableListOf<Any>()
        every { domainEventPublisher.publish(capture(capturedEvents)) } just runs

        // When
        service.execute(aCreateCommand())

        // Then
        val publishedEvent = capturedEvents.first()
        assertTrue(publishedEvent is EventCreatedEvent)
        assertEquals(savedEvent.id, (publishedEvent as EventCreatedEvent).eventId)
    }

    // AC: GH-3-AC-7 — TEST-A02: findEvent returns event when found
    @Test
    fun `findEvent returns event when found`() = runTest {
        // Given
        val event = anEvent()
        coEvery { eventRepository.findById(event.id) } returns event

        // When
        val result = service.execute(FindEventQuery(event.id))

        // Then
        assertEquals(event, result)
    }

    // AC: GH-3-AC-7 — TEST-A03: findEvent returns null when event does not exist
    @Test
    fun `findEvent returns null when not found`() = runTest {
        // Given
        val id = EventId.generate()
        coEvery { eventRepository.findById(id) } returns null

        // When
        val result = service.execute(FindEventQuery(id))

        // Then
        assertNull(result)
    }

    // AC: GH-3-AC-7 — TEST-A04: listEvents delegates page/size to repository
    @Test
    fun `listEvents delegates to repository`() = runTest {
        // Given
        val events = listOf(anEvent(), anEvent())
        coEvery { eventRepository.findAll(0, 20) } returns events

        // When
        val result = service.executeAll(ListEventsQuery(0, 20))

        // Then
        assertEquals(2, result.size)
        coVerify(exactly = 1) { eventRepository.findAll(0, 20) }
    }

    // AC: GH-3-AC-7 — TEST-A06: BVA — size=1 returns exactly one event
    @Test
    fun `listEvents with size 1 returns single event`() = runTest {
        // Given
        val singleEvent = anEvent()
        coEvery { eventRepository.findAll(0, 1) } returns listOf(singleEvent)

        // When
        val result = service.executeAll(ListEventsQuery(0, 1))

        // Then
        assertEquals(1, result.size)
        assertEquals(singleEvent.id, result.first().id)
    }

    // AC: GH-3-AC-7 — edge case: listEvents returns empty list when repository is empty
    @Test
    fun `listEvents returns empty list when no events exist`() = runTest {
        // Given — TEST-S01 equivalent at application layer
        coEvery { eventRepository.findAll(0, 20) } returns emptyList()

        // When
        val result = service.executeAll(ListEventsQuery(0, 20))

        // Then
        assertTrue(result.isEmpty())
    }

    // AC: GH-3-AC-7 — TEST-A05: recommendEvents filters and ranks events
    @Test
    fun `recommendEvents filters and ranks events`() = runTest {
        // Given
        val techEvent = anEvent()
        coEvery {
            eventRepository.findByCategories(setOf(Category.TECHNOLOGY), 30)
        } returns listOf(techEvent)

        // When
        val result = service.execute(
            RecommendEventsQuery(
                preferredCategories = setOf(Category.TECHNOLOGY),
                userLocation = berlin,
                maxDistanceKm = 100.0,
                limit = 10,
            ),
        )

        // Then
        assertEquals(1, result.size)
        assertEquals(techEvent.id, result.first().id)
    }

    // AC: GH-3-AC-7 — edge case: recommendEvents with empty categories returns all candidates
    @Test
    fun `recommendEvents with empty categories returns all candidates`() = runTest {
        // Given — cold-start: no preference categories
        val event1 = anEvent()
        val event2 = anEvent()
        coEvery { eventRepository.findByCategories(emptySet(), 30) } returns listOf(event1, event2)

        // When
        val result = service.execute(
            RecommendEventsQuery(
                preferredCategories = emptySet(),
                userLocation = null,
                maxDistanceKm = null,
                limit = 10,
            ),
        )

        // Then — cold start: no category filter applied, all events returned
        assertEquals(2, result.size)
    }
}
