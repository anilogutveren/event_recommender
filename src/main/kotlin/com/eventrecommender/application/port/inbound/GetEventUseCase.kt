package com.eventrecommender.application.port.inbound

import com.eventrecommender.domain.model.Event
import com.eventrecommender.domain.model.EventId

interface GetEventUseCase {
    suspend fun execute(query: FindEventQuery): Event?
    suspend fun executeAll(query: ListEventsQuery): List<Event>
}

data class FindEventQuery(val id: EventId)

data class ListEventsQuery(
    val page: Int = 0,
    val size: Int = 20,
)
