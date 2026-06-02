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

    // AC: GH-3-AC-7 — TEST-S01: edge case — empty catalogue returns empty list
    @Test
    fun `filterByPreferences returns empty list when catalogue is empty`() {
        val result = service.filterByPreferences(emptyList(), setOf(Category.MUSIC), null, null)
        assertTrue(result.isEmpty())
    }

    // AC: GH-3-AC-7 — TEST-S02: cold start — no category preference returns all events
    @Test
    fun `filterByPreferences returns all events when categories empty`() {
        val events = listOf(makeEvent(Category.MUSIC), makeEvent(Category.SPORTS))
        val result = service.filterByPreferences(events, emptySet(), null, null)
        assertEquals(2, result.size)
    }

    // AC: GH-3-AC-7 — TEST-S03: equivalence partition — exact category match
    @Test
    fun `filterByPreferences filters by category`() {
        val music = makeEvent(Category.MUSIC)
        val sports = makeEvent(Category.SPORTS)
        val result = service.filterByPreferences(listOf(music, sports), setOf(Category.MUSIC), null, null)
        assertEquals(listOf(music), result)
    }

    // AC: GH-3-AC-7 — TEST-S04: BVA — events within maxDistanceKm are included
    @Test
    fun `filterByPreferences includes event within distance threshold`() {
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

    // AC: GH-3-AC-7 — TEST-S05: all past events — filterByPreferences has no date filter itself
    @Test
    fun `filterByPreferences does not filter by date, returns past events`() {
        val pastEvent = Event.create(
            title = "Past Event",
            description = "Old event",
            category = Category.TECHNOLOGY,
            location = berlin,
            venue = "Venue",
            startTime = Instant.now().minus(3, ChronoUnit.DAYS),
            endTime = Instant.now().minus(1, ChronoUnit.DAYS),
        )
        val result = service.filterByPreferences(listOf(pastEvent), emptySet(), null, null)
        assertEquals(1, result.size) // date filtering is responsibility of repository query
    }

    // AC: GH-3-AC-7 — TEST-S06: edge case — rank of empty list returns empty list
    @Test
    fun `rank returns empty list when no events provided`() {
        val result = service.rank(emptyList(), null)
        assertTrue(result.isEmpty())
    }

    // AC: GH-3-AC-7 — TEST-S07: upcoming events ranked before past events
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

    // AC: GH-3-AC-7 — TEST-S08: geo proximity — closer event ranked first
    @Test
    fun `rank sorts by geo proximity when userLocation provided`() {
        val nearEvent = makeEvent(location = berlin, title = "Near")
        val farEvent = makeEvent(location = tokyo, title = "Far")
        val ranked = service.rank(listOf(farEvent, nearEvent), munich)
        assertEquals("Near", ranked.first().title)
    }

    // AC: GH-3-AC-7 — TEST-S09: Decision Table row 1 — matches category AND within radius → included
    @Test
    fun `filterByPreferences includes event matching category AND within radius`() {
        val included = makeEvent(category = Category.MUSIC, location = berlin, title = "Included")
        val result = service.filterByPreferences(
            listOf(included),
            setOf(Category.MUSIC),
            userLocation = munich,
            maxDistanceKm = 600.0,
        )
        assertEquals(1, result.size)
        assertEquals("Included", result.first().title)
    }

    // AC: GH-3-AC-7 — TEST-S10: Decision Table row 2 — matches category but outside radius → excluded
    @Test
    fun `filterByPreferences excludes event matching category but outside radius`() {
        val excluded = makeEvent(category = Category.MUSIC, location = tokyo, title = "Excluded")
        val result = service.filterByPreferences(
            listOf(excluded),
            setOf(Category.MUSIC),
            userLocation = munich,
            maxDistanceKm = 600.0,
        )
        assertTrue(result.isEmpty())
    }

    // AC: GH-3-AC-7 — TEST-S11: Decision Table row 3 — wrong category even if within radius → excluded
    @Test
    fun `filterByPreferences excludes event wrong category even if within radius`() {
        val excluded = makeEvent(category = Category.SPORTS, location = berlin, title = "Excluded")
        val result = service.filterByPreferences(
            listOf(excluded),
            setOf(Category.MUSIC),
            userLocation = munich,
            maxDistanceKm = 600.0,
        )
        assertTrue(result.isEmpty())
    }

    // AC: GH-3-AC-7 — rank is stable for single event
    @Test
    fun `rank with single event returns that event`() {
        val single = makeEvent(title = "Solo")
        val result = service.rank(listOf(single), null)
        assertEquals(1, result.size)
        assertEquals("Solo", result.first().title)
    }
}
