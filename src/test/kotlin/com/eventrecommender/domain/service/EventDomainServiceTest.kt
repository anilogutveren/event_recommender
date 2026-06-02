package com.eventrecommender.domain.service

import com.eventrecommender.domain.model.Category
import com.eventrecommender.domain.model.Event
import com.eventrecommender.domain.model.Location
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class EventDomainServiceTest {

    private val service = EventDomainService()

    private val berlin = Location(52.52, 13.405, "Berlin", "Germany")
    private val munich = Location(48.137, 11.576, "Munich", "Germany")
    private val tokyo = Location(35.689, 139.692, "Tokyo", "Japan")

    private fun makeEvent(
        category: Category = Category.TECHNOLOGY,
        location: Location = berlin,
        title: String = "Event",
        startOffset: Long = 1L,
    ): Event = Event.create(
        title = title,
        description = "Desc",
        category = category,
        location = location,
        venue = "Venue",
        startTime = Instant.now().plus(startOffset, ChronoUnit.DAYS),
        endTime = Instant.now().plus(startOffset + 1, ChronoUnit.DAYS),
    )

    @Test
    fun `filterByPreferences returns all events when categories empty`() {
        val events = listOf(makeEvent(Category.MUSIC), makeEvent(Category.SPORTS))
        val result = service.filterByPreferences(events, emptySet(), null, null)
        assertEquals(2, result.size)
    }

    @Test
    fun `filterByPreferences filters by category`() {
        val music = makeEvent(Category.MUSIC)
        val sports = makeEvent(Category.SPORTS)
        val result = service.filterByPreferences(listOf(music, sports), setOf(Category.MUSIC), null, null)
        assertEquals(listOf(music), result)
    }

    @Test
    fun `filterByPreferences filters by distance`() {
        val nearEvent = makeEvent(location = berlin, title = "Near")
        val farEvent = makeEvent(location = tokyo, title = "Far")
        val result = service.filterByPreferences(
            listOf(nearEvent, farEvent),
            emptySet(),
            userLocation = munich,
            maxDistanceKm = 600.0,
        )
        assertTrue(nearEvent in result)
        assertTrue(farEvent !in result)
    }

    @Test
    fun `rank puts upcoming events first`() {
        val upcoming = makeEvent(title = "Upcoming", startOffset = 1)
        val past = Event.create(
            title = "Past",
            description = "Desc",
            category = Category.TECHNOLOGY,
            location = berlin,
            venue = "Venue",
            startTime = Instant.now().minus(2, ChronoUnit.DAYS),
            endTime = Instant.now().minus(1, ChronoUnit.DAYS),
        )
        val ranked = service.rank(listOf(past, upcoming), null)
        assertEquals("Upcoming", ranked.first().title)
    }

    @Test
    fun `rank sorts by geo proximity when userLocation provided`() {
        val nearEvent = makeEvent(location = berlin, title = "Near")
        val farEvent = makeEvent(location = tokyo, title = "Far")
        val ranked = service.rank(listOf(farEvent, nearEvent), munich)
        assertEquals("Near", ranked.first().title)
    }
}
