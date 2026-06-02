package com.eventrecommender.adapter.outbound.monitoring

import com.eventrecommender.application.port.outbound.EventMetricsPort
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

/**
 * MicrometerEventMetrics — driven adapter implementing EventMetricsPort.
 *
 * Adapter Pattern: adapts Micrometer to the domain's EventMetricsPort interface.
 * Custom metrics as defined in ADR-0003:
 *   - es.query.duration  (Timer)
 *   - es.index.operations (Counter)
 *   - es.documents.total  (Gauge — updated on demand)
 */
@Component
class MicrometerEventMetrics(
    private val meterRegistry: MeterRegistry,
) : EventMetricsPort {

    private val queryTimer: Timer = Timer.builder("es.query.duration")
        .description("Duration of Elasticsearch query operations in milliseconds")
        .register(meterRegistry)

    private val indexCounter = meterRegistry.counter(
        "es.index.operations",
        "description", "Number of Elasticsearch index (write) operations",
    )

    private var documentCount: Long = 0L

    init {
        meterRegistry.gauge("es.documents.total", this) { it.documentCount.toDouble() }
    }

    override fun recordQueryDuration(durationMs: Long) {
        queryTimer.record(durationMs, TimeUnit.MILLISECONDS)
    }

    override fun incrementIndexOperation() {
        indexCounter.increment()
    }

    override fun setDocumentCount(count: Long) {
        documentCount = count
    }
}
