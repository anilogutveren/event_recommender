package com.eventrecommender.application.port.outbound

/**
 * DomainEventPublisher — outbound port for publishing domain events.
 */
interface DomainEventPublisher {
    fun publish(event: Any)
}
