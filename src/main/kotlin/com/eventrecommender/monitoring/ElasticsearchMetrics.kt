package com.eventrecommender.monitoring

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.query.StringQuery
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Publishes Elasticsearch operational metrics to Micrometer.
 *
 * Metrics emitted:
 *  - es.query.duration     (Timer)   — latency of ES search operations
 *  - es.index.operations   (Counter) — total indexing calls made
 *  - es.documents.total    (Gauge)   — approximate document count in the default index
 */
@Component
class ElasticsearchMetrics(
    private val meterRegistry: MeterRegistry,
    private val elasticsearchOperations: ElasticsearchOperations,
) {
    val queryTimer: Timer = Timer.builder("es.query.duration")
        .description("Latency of Elasticsearch search operations")
        .tag("service", "event_recommender")
        .register(meterRegistry)

    val indexOperationCounter: Counter = Counter.builder("es.index.operations")
        .description("Total number of Elasticsearch index operations")
        .tag("service", "event_recommender")
        .register(meterRegistry)

    @Scheduled(fixedDelayString = "\${monitoring.es.document-count-interval-ms:30000}")
    fun refreshDocumentCountGauge() {
        runCatching {
            val count = elasticsearchOperations
                .count(StringQuery("{\"match_all\": {}}"), Any::class.java)
            meterRegistry.gauge("es.documents.total", count.toDouble())
        }.onFailure {
            meterRegistry.gauge("es.documents.total", -1.0)
        }
    }
}
