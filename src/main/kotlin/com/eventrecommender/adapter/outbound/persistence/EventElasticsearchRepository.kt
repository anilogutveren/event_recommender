package com.eventrecommender.adapter.outbound.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

/**
 * Spring Data Elasticsearch repository interface — infrastructure detail, stays in adapter layer.
 */
interface EventElasticsearchRepository : ElasticsearchRepository<EventDocument, String> {
    fun findByCategoryIn(categories: List<String>, pageable: Pageable): Page<EventDocument>
}
