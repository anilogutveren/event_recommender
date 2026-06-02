package com.eventrecommender.application.port.inbound

import com.eventrecommender.domain.model.Category
import com.eventrecommender.domain.model.Event
import com.eventrecommender.domain.model.EventTag
import com.eventrecommender.domain.model.Location
import java.time.Instant

interface CreateEventUseCase {
    suspend fun execute(command: CreateEventCommand): Event
}

data class CreateEventCommand(
    val title: String,
    val description: String,
    val category: Category,
    val location: Location,
    val venue: String,
    val startTime: Instant,
    val endTime: Instant,
    val tags: Set<EventTag> = emptySet(),
)
