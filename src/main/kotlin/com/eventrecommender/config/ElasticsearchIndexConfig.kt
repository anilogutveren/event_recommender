package com.eventrecommender.config

import com.eventrecommender.adapter.outbound.persistence.EventDocument
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.IndexOperations

/**
 * Handles Elasticsearch index creation after application startup.
 *
 * Since `@Document(createIndex = false)` disables auto-creation at bean instantiation,
 * this config ensures the index and mapping are created when Elasticsearch is available
 * without preventing the application from starting.
 */
@Configuration
@Profile("!test")
class ElasticsearchIndexConfig(
    private val elasticsearchOperations: ElasticsearchOperations,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener(ApplicationReadyEvent::class)
    fun createIndexIfNeeded() {
        try {
            val indexOps: IndexOperations = elasticsearchOperations.indexOps(EventDocument::class.java)
            if (!indexOps.exists()) {
                indexOps.createWithMapping()
                log.info("Created Elasticsearch index 'events' with mapping")
            } else {
                log.info("Elasticsearch index 'events' already exists")
            }
        } catch (ex: Exception) {
            log.warn(
                "Could not verify/create Elasticsearch index 'events' — " +
                    "the index will be created on first write if Elasticsearch becomes available. " +
                    "Cause: {}",
                ex.message,
            )
        }
    }
}

