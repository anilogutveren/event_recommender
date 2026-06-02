package com.eventrecommender.adapter.outbound.persistence

import com.eventrecommender.application.port.outbound.EventRepository
import com.eventrecommender.domain.model.Category
import com.eventrecommender.domain.model.Event
import com.eventrecommender.domain.model.EventId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

/**
 * ElasticsearchEventRepositoryAdapter — driven adapter implementing the EventRepository outbound port.
 *
 * Adapter Pattern: adapts Spring Data ES to the domain's EventRepository interface.
 * Maps between EventDocument (persistence model) and Event (domain model).
 */
@Component
@Profile("!test")
class ElasticsearchEventRepositoryAdapter(
    private val esRepository: EventElasticsearchRepository,
) : EventRepository {

    override suspend fun save(event: Event): Event = withContext(Dispatchers.IO) {
        val document = EventDocument.fromDomain(event)
        val saved = esRepository.save(document)
        saved.toDomain()
    }

    override suspend fun findById(id: EventId): Event? = withContext(Dispatchers.IO) {
        esRepository.findById(id.value).orElse(null)?.toDomain()
    }

    override suspend fun findAll(page: Int, size: Int): List<Event> = withContext(Dispatchers.IO) {
        esRepository.findAll(PageRequest.of(page, size))
            .content
            .map { it.toDomain() }
    }

    override suspend fun findByCategories(categories: Set<Category>, limit: Int): List<Event> =
        withContext(Dispatchers.IO) {
            val categoryNames = if (categories.isEmpty()) {
                Category.entries.map { it.name }
            } else {
                categories.map { it.name }
            }
            esRepository.findByCategoryIn(categoryNames, PageRequest.of(0, limit))
                .content
                .map { it.toDomain() }
        }

    override suspend fun delete(id: EventId): Unit = withContext(Dispatchers.IO) {
        esRepository.deleteById(id.value)
    }

    override suspend fun count(): Long = withContext(Dispatchers.IO) {
        esRepository.count()
    }
}
