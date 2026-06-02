package com.eventrecommender.domain.model

import java.time.Instant

/**
 * Event — the core domain entity.
 *
 * Rules:
 * - No framework annotations
 * - Immutable (data class)
 * - Business rules enforced in init / companion object
 */
data class Event(
    val id: EventId,
    val title: String,
    val description: String,
    val category: Category,
    val location: Location,
    val venue: String,
    val startTime: Instant,
    val endTime: Instant,
    val tags: Set<EventTag>,
    val createdAt: Instant,
) {
    init {
        require(title.isNotBlank()) { "Event title must not be blank" }
        require(description.isNotBlank()) { "Event description must not be blank" }
        require(venue.isNotBlank()) { "Venue must not be blank" }
        require(endTime.isAfter(startTime)) { "Event endTime must be after startTime" }
    }

    fun isUpcoming(now: Instant = Instant.now()): Boolean = startTime.isAfter(now)

    fun hasTag(tag: EventTag): Boolean = tag in tags

    fun addTag(tag: EventTag): Event = copy(tags = tags + tag)

    fun removeTag(tag: EventTag): Event = copy(tags = tags - tag)

    fun update(
        title: String = this.title,
        description: String = this.description,
        venue: String = this.venue,
        startTime: Instant = this.startTime,
        endTime: Instant = this.endTime,
        tags: Set<EventTag> = this.tags,
    ): Event = copy(
        title = title,
        description = description,
        venue = venue,
        startTime = startTime,
        endTime = endTime,
        tags = tags,
    )

    companion object {
        fun create(
            title: String,
            description: String,
            category: Category,
            location: Location,
            venue: String,
            startTime: Instant,
            endTime: Instant,
            tags: Set<EventTag> = emptySet(),
        ): Event = Event(
            id = EventId.generate(),
            title = title,
            description = description,
            category = category,
            location = location,
            venue = venue,
            startTime = startTime,
            endTime = endTime,
            tags = tags,
            createdAt = Instant.now(),
        )
    }
}
