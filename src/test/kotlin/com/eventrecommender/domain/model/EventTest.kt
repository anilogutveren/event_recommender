package com.eventrecommender.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.time.temporal.ChronoUnit

class EventTest {

    private val futureStart = Instant.now().plus(1, ChronoUnit.DAYS)
    private val futureEnd = Instant.now().plus(2, ChronoUnit.DAYS)
    private val pastStart = Instant.now().minus(2, ChronoUnit.DAYS)
    private val pastEnd = Instant.now().minus(1, ChronoUnit.DAYS)

    private fun aLocation() = Location(52.52, 13.405, "Berlin", "Germany")

    private fun anEvent(
        title: String = "Tech Conference",
        description: String = "A tech conference in Berlin",
        venue: String = "Berlin Expo Center",
        startTime: Instant = futureStart,
        endTime: Instant = futureEnd,
    ) = Event.create(
        title = title,
        description = description,
        category = Category.TECHNOLOGY,
        location = aLocation(),
        venue = venue,
        startTime = startTime,
        endTime = endTime,
    )

    // AC: GH-3-AC-2 — Domain entity enforces business rules: unique id on creation
    @Test
    fun `create generates a unique id`() {
        val e1 = anEvent()
        val e2 = anEvent()
        assertTrue(e1.id.value.isNotBlank())
        assertNotEquals(e1.id, e2.id)
    }

    // AC: GH-3-AC-2 — Domain entity enforces: title must not be blank
    @Test
    fun `create fails when title is blank`() {
        // TEST-D01
        assertThrows<IllegalArgumentException> { anEvent(title = " ") }
    }

    // AC: GH-3-AC-2 — Domain entity enforces: description must not be blank
    @Test
    fun `create fails when description is blank`() {
        // TEST-D02
        assertThrows<IllegalArgumentException> { anEvent(description = " ") }
    }

    // AC: GH-3-AC-2 — Domain entity enforces: venue must not be blank
    @Test
    fun `create fails when venue is blank`() {
        // TEST-D03
        assertThrows<IllegalArgumentException> { anEvent(venue = "   ") }
    }

    // AC: GH-3-AC-2 — Boundary value: endTime must be strictly after startTime
    @Test
    fun `create fails when endTime is before startTime`() {
        // TEST-D04
        assertThrows<IllegalArgumentException> { anEvent(startTime = futureEnd, endTime = futureStart) }
    }

    // AC: GH-3-AC-2 — Boundary value: endTime == startTime is invalid
    @Test
    fun `create fails when endTime equals startTime`() {
        // TEST-D04 BVA boundary
        assertThrows<IllegalArgumentException> { anEvent(startTime = futureStart, endTime = futureStart) }
    }

    @Test
    fun `isUpcoming returns true for future events`() {
        assertTrue(anEvent(startTime = futureStart, endTime = futureEnd).isUpcoming())
    }

    @Test
    fun `isUpcoming returns false for past events`() {
        assertFalse(anEvent(startTime = pastStart, endTime = pastEnd).isUpcoming())
    }

    // AC: GH-3-AC-2 — TEST-D06
    @Test
    fun `addTag adds tag to event`() {
        val tag = EventTag("kotlin")
        val event = anEvent().addTag(tag)
        assertTrue(event.hasTag(tag))
    }

    // AC: GH-3-AC-2 — TEST-D07
    @Test
    fun `removeTag removes tag from event`() {
        val tag = EventTag("kotlin")
        val event = anEvent().addTag(tag).removeTag(tag)
        assertFalse(event.hasTag(tag))
    }

    // AC: GH-3-AC-2 — TEST-D08
    @Test
    fun `update returns new event with modified fields preserving id`() {
        val original = anEvent(title = "Original")
        val updated = original.update(title = "Updated")
        assertEquals("Updated", updated.title)
        assertEquals(original.id, updated.id)
        assertEquals(original.category, updated.category)
    }

    // AC: GH-3-AC-2 — update is immutable: original unchanged
    @Test
    fun `update does not mutate original event`() {
        val original = anEvent(title = "Original")
        original.update(title = "Updated")
        assertEquals("Original", original.title)
    }
}
