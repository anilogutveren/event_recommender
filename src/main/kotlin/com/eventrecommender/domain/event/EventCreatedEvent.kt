package com.eventrecommender.domain.event

import com.eventrecommender.domain.model.EventId
import java.time.Instant

data class EventCreatedEvent(
    val eventId: EventId,
    val occurredAt: Instant = Instant.now(),
)
