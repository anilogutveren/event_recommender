package com.eventrecommender.adapter.inbound.rest

import com.eventrecommender.domain.model.Event
import java.time.Instant

data class EventResponse(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val city: String,
    val country: String,
    val venue: String,
    val startTime: Instant,
    val endTime: Instant,
    val tags: Set<String>,
    val createdAt: Instant,
) {
    companion object {
        fun fromDomain(event: Event): EventResponse = EventResponse(
            id = event.id.value,
            title = event.title,
            description = event.description,
            category = event.category.name,
            latitude = event.location.latitude,
            longitude = event.location.longitude,
            city = event.location.city,
            country = event.location.country,
            venue = event.venue,
            startTime = event.startTime,
            endTime = event.endTime,
            tags = event.tags.map { it.value }.toSet(),
            createdAt = event.createdAt,
        )
    }
}
