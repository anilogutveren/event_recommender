package com.eventrecommender.adapter.inbound.rest

import com.eventrecommender.application.port.inbound.CreateEventCommand
import com.eventrecommender.domain.model.Category
import com.eventrecommender.domain.model.EventTag
import com.eventrecommender.domain.model.Location
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import java.time.Instant

data class CreateEventRequest(
    val title: String,
    val description: String,
    val category: Category,
    val latitude: Double,
    val longitude: Double,
    val city: String,
    val country: String,
    val venue: String,
    val startTime: Instant,
    val endTime: Instant,
    @field:JsonSetter(nulls = Nulls.AS_EMPTY)
    val tags: Set<String> = emptySet(),
) {
    fun toCommand(): CreateEventCommand = CreateEventCommand(
        title = title,
        description = description,
        category = category,
        location = Location(
            latitude = latitude,
            longitude = longitude,
            city = city,
            country = country,
        ),
        venue = venue,
        startTime = startTime,
        endTime = endTime,
        tags = tags.map { EventTag(it) }.toSet(),
    )
}

data class RecommendEventsRequest(
    val categories: Set<Category> = emptySet(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val city: String? = null,
    val country: String? = null,
    val maxDistanceKm: Double? = null,
    val limit: Int = 10,
)
