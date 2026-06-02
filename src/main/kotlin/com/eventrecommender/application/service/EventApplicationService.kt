package com.eventrecommender.application.service

import com.eventrecommender.application.port.inbound.CreateEventCommand
import com.eventrecommender.application.port.inbound.CreateEventUseCase
import com.eventrecommender.application.port.inbound.FindEventQuery
import com.eventrecommender.application.port.inbound.GetEventUseCase
import com.eventrecommender.application.port.inbound.ListEventsQuery
import com.eventrecommender.application.port.inbound.RecommendEventsQuery
import com.eventrecommender.application.port.inbound.RecommendEventsUseCase
import com.eventrecommender.application.port.outbound.DomainEventPublisher
import com.eventrecommender.application.port.outbound.EventMetricsPort
import com.eventrecommender.application.port.outbound.EventRepository
import com.eventrecommender.domain.event.EventCreatedEvent
import com.eventrecommender.domain.model.Event
import com.eventrecommender.domain.service.EventDomainService
import org.slf4j.LoggerFactory

/**
 * EventApplicationService — orchestrates domain logic for event use cases.
 *
 * Rules:
 * - Implements Inbound Ports (CreateEventUseCase, GetEventUseCase, RecommendEventsUseCase)
 * - Uses Outbound Ports (EventRepository, EventMetricsPort, DomainEventPublisher)
 * - No business logic — delegates to EventDomainService
 * - No framework annotations (Spring wiring is done via adapter configuration)
 */
class EventApplicationService(
    private val eventRepository: EventRepository,
    private val eventMetrics: EventMetricsPort,
    private val eventPublisher: DomainEventPublisher,
    private val domainService: EventDomainService,
) : CreateEventUseCase, GetEventUseCase, RecommendEventsUseCase {

    private val log = LoggerFactory.getLogger(EventApplicationService::class.java)

    override suspend fun execute(command: CreateEventCommand): Event {
        log.info("Creating event: title={}", command.title)

        val event = Event.create(
            title = command.title,
            description = command.description,
            category = command.category,
            location = command.location,
            venue = command.venue,
            startTime = command.startTime,
            endTime = command.endTime,
            tags = command.tags,
        )

        val startMs = System.currentTimeMillis()
        val saved = eventRepository.save(event)
        eventMetrics.recordQueryDuration(System.currentTimeMillis() - startMs)
        eventMetrics.incrementIndexOperation()

        eventPublisher.publish(EventCreatedEvent(saved.id))

        log.info("Event created: id={}", saved.id.value)
        return saved
    }

    override suspend fun execute(query: FindEventQuery): Event? {
        log.debug("Finding event by id={}", query.id.value)
        val startMs = System.currentTimeMillis()
        val result = eventRepository.findById(query.id)
        eventMetrics.recordQueryDuration(System.currentTimeMillis() - startMs)
        return result
    }

    override suspend fun executeAll(query: ListEventsQuery): List<Event> {
        log.debug("Listing events: page={}, size={}", query.page, query.size)
        val startMs = System.currentTimeMillis()
        val result = eventRepository.findAll(query.page, query.size)
        eventMetrics.recordQueryDuration(System.currentTimeMillis() - startMs)
        return result
    }

    override suspend fun execute(query: RecommendEventsQuery): List<Event> {
        log.debug("Recommending events: categories={}, location={}", query.preferredCategories, query.userLocation)

        val startMs = System.currentTimeMillis()
        val candidates = eventRepository.findByCategories(query.preferredCategories, query.limit * 3)
        eventMetrics.recordQueryDuration(System.currentTimeMillis() - startMs)

        val filtered = domainService.filterByPreferences(
            events = candidates,
            categories = query.preferredCategories,
            userLocation = query.userLocation,
            maxDistanceKm = query.maxDistanceKm,
        )

        return domainService.rank(filtered, query.userLocation).take(query.limit)
    }
}
