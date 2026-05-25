package com.eventrecommender.monitoring

import co.elastic.clients.elasticsearch.ElasticsearchClient
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Publishes Elasticsearch operational metrics to Micrometer.
 *
 * Metrics emitted:
 *  - es.query.duration     (Timer)   — latency of ES search operations
 *  - es.index.operations   (Counter) — total indexing calls made
 *  - es.documents.total    (Gauge)   — approximate document count across all indices
 */
@Component
class ElasticsearchMetrics(
    private val meterRegistry: MeterRegistry,
    private val client: ElasticsearchClient,
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
            val count = client.count { c -> c.index("*") }.count()
            meterRegistry.gauge("es.documents.total", count.toDouble())
        }.onFailure {
            meterRegistry.gauge("es.documents.total", -1.0)
        }
    }
}
