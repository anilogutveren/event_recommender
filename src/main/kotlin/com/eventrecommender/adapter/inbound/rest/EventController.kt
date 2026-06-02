package com.eventrecommender.adapter.inbound.rest

import com.eventrecommender.application.port.inbound.CreateEventUseCase
import com.eventrecommender.application.port.inbound.FindEventQuery
import com.eventrecommender.application.port.inbound.GetEventUseCase
import com.eventrecommender.application.port.inbound.ListEventsQuery
import com.eventrecommender.application.port.inbound.RecommendEventsQuery
import com.eventrecommender.application.port.inbound.RecommendEventsUseCase
import com.eventrecommender.domain.model.EventId
import com.eventrecommender.domain.model.Location
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * EventController — driving (inbound) adapter.
 *
 * Translates HTTP requests to Use Case commands/queries.
 * Depends only on Inbound Port interfaces — never on implementation classes.
 * Uses runBlocking to bridge coroutines with Spring MVC (non-reactive stack).
 */
@RestController
@RequestMapping("/api/v1/events")
class EventController(
    private val createEventUseCase: CreateEventUseCase,
    private val getEventUseCase: GetEventUseCase,
    private val recommendEventsUseCase: RecommendEventsUseCase,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createEvent(@RequestBody request: CreateEventRequest): EventResponse = runBlocking {
        val event = createEventUseCase.execute(request.toCommand())
        EventResponse.fromDomain(event)
    }

    @GetMapping("/{id}")
    fun getEvent(@PathVariable id: String): ResponseEntity<EventResponse> = runBlocking {
        val event = getEventUseCase.execute(FindEventQuery(EventId.of(id)))
        if (event != null) ResponseEntity.ok(EventResponse.fromDomain(event))
        else ResponseEntity.notFound().build()
    }

    @GetMapping
    fun listEvents(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): List<EventResponse> = runBlocking {
        getEventUseCase.executeAll(ListEventsQuery(page, size))
            .map { EventResponse.fromDomain(it) }
    }

    @PostMapping("/recommendations")
    fun recommend(@RequestBody request: RecommendEventsRequest): List<EventResponse> = runBlocking {
        val location = if (request.latitude != null && request.longitude != null &&
            request.city != null && request.country != null
        ) {
            Location(request.latitude, request.longitude, request.city, request.country)
        } else {
            null
        }

        recommendEventsUseCase.execute(
            RecommendEventsQuery(
                preferredCategories = request.categories,
                userLocation = location,
                maxDistanceKm = request.maxDistanceKm,
                limit = request.limit,
            ),
        ).map { EventResponse.fromDomain(it) }
    }
}
