package com.eventrecommender.application.port.inbound

import com.eventrecommender.domain.model.Category
import com.eventrecommender.domain.model.Event
import com.eventrecommender.domain.model.Location

interface RecommendEventsUseCase {
    suspend fun execute(query: RecommendEventsQuery): List<Event>
}

data class RecommendEventsQuery(
    val preferredCategories: Set<Category> = emptySet(),
    val userLocation: Location? = null,
    val maxDistanceKm: Double? = null,
    val limit: Int = 10,
)
