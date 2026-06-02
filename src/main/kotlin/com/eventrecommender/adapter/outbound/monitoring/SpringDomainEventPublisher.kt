package com.eventrecommender.adapter.outbound.monitoring

import com.eventrecommender.application.port.outbound.DomainEventPublisher
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

/**
 * SpringDomainEventPublisher — driven adapter implementing DomainEventPublisher.
 *
 * Uses Spring's ApplicationEventPublisher so domain events can be consumed
 * by any Spring @EventListener without coupling the application service to Spring.
 */
@Component
class SpringDomainEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher,
) : DomainEventPublisher {

    private val log = LoggerFactory.getLogger(SpringDomainEventPublisher::class.java)

    override fun publish(event: Any) {
        log.debug("Publishing domain event: {}", event::class.simpleName)
        applicationEventPublisher.publishEvent(event)
    }
}
