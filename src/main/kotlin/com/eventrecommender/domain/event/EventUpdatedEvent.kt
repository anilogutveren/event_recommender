package com.eventrecommender.domain.event

import com.eventrecommender.domain.model.EventId
import java.time.Instant

data class EventUpdatedEvent(
    val eventId: EventId,
    val occurredAt: Instant = Instant.now(),
)
