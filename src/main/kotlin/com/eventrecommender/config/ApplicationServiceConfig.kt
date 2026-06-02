package com.eventrecommender.config

import com.eventrecommender.application.port.outbound.DomainEventPublisher
import com.eventrecommender.application.port.outbound.EventMetricsPort
import com.eventrecommender.application.port.outbound.EventRepository
import com.eventrecommender.application.service.EventApplicationService
import com.eventrecommender.domain.service.EventDomainService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * ApplicationServiceConfig — Spring configuration that wires the hexagonal layers.
 *
 * The application service (EventApplicationService) has no Spring annotations itself.
 * Spring wiring is done here in the config layer, keeping the application core clean.
 */
@Configuration
class ApplicationServiceConfig {

    @Bean
    fun eventDomainService(): EventDomainService = EventDomainService()

    @Bean
    fun eventApplicationService(
        eventRepository: EventRepository,
        eventMetrics: EventMetricsPort,
        eventPublisher: DomainEventPublisher,
        domainService: EventDomainService,
    ): EventApplicationService = EventApplicationService(
        eventRepository = eventRepository,
        eventMetrics = eventMetrics,
        eventPublisher = eventPublisher,
        domainService = domainService,
    )
}
