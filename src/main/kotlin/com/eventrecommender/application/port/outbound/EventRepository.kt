package com.eventrecommender.application.port.outbound

import com.eventrecommender.domain.model.Category
import com.eventrecommender.domain.model.Event
import com.eventrecommender.domain.model.EventId

/**
 * EventRepository — outbound port for event persistence.
 *
 * Domain defines this interface; infrastructure adapters implement it.
 * Only domain models cross this boundary — no persistence entities.
 */
interface EventRepository {
    suspend fun save(event: Event): Event
    suspend fun findById(id: EventId): Event?
    suspend fun findAll(page: Int, size: Int): List<Event>
    suspend fun findByCategories(categories: Set<Category>, limit: Int): List<Event>
    suspend fun delete(id: EventId)
    suspend fun count(): Long
}
