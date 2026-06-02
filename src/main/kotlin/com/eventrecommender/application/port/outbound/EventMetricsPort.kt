package com.eventrecommender.application.port.outbound

/**
 * EventMetricsPort — outbound port for recording observability signals.
 *
 * Decouples the application service from the concrete metrics library (Micrometer).
 */
interface EventMetricsPort {
    fun recordQueryDuration(durationMs: Long)
    fun incrementIndexOperation()
    fun setDocumentCount(count: Long)
}
