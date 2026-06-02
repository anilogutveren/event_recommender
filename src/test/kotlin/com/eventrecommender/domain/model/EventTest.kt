package com.eventrecommender.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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
        startTime: Instant = futureStart,
        endTime: Instant = futureEnd,
    ) = Event.create(
        title = title,
        description = "A tech conference in Berlin",
        category = Category.TECHNOLOGY,
        location = aLocation(),
        venue = "Berlin Expo Center",
        startTime = startTime,
        endTime = endTime,
    )

    @Test
    fun `create generates a unique id`() {
        val e1 = anEvent()
        val e2 = anEvent()
        assertTrue(e1.id.value.isNotBlank())
        assertTrue(e1.id != e2.id)
    }

    @Test
    fun `create fails when title is blank`() {
        assertThrows<IllegalArgumentException> { anEvent(title = " ") }
    }

    @Test
    fun `create fails when endTime is before startTime`() {
        assertThrows<IllegalArgumentException> { anEvent(startTime = futureEnd, endTime = futureStart) }
    }

    @Test
    fun `isUpcoming returns true for future events`() {
        assertTrue(anEvent(startTime = futureStart, endTime = futureEnd).isUpcoming())
    }

    @Test
    fun `isUpcoming returns false for past events`() {
        assertFalse(anEvent(startTime = pastStart, endTime = pastEnd).isUpcoming())
    }

    @Test
    fun `addTag adds tag to event`() {
        val tag = EventTag("kotlin")
        val event = anEvent().addTag(tag)
        assertTrue(event.hasTag(tag))
    }

    @Test
    fun `removeTag removes tag from event`() {
        val tag = EventTag("kotlin")
        val event = anEvent().addTag(tag).removeTag(tag)
        assertFalse(event.hasTag(tag))
    }

    @Test
    fun `update returns new event with modified fields`() {
        val original = anEvent(title = "Original")
        val updated = original.update(title = "Updated")
        assertEquals("Updated", updated.title)
        assertEquals(original.id, updated.id)
        assertEquals(original.category, updated.category)
    }
}
