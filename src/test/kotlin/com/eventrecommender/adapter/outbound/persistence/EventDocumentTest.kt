package com.eventrecommender.adapter.outbound.persistence

import com.eventrecommender.domain.model.Category
import com.eventrecommender.domain.model.Event
import com.eventrecommender.domain.model.EventId
import com.eventrecommender.domain.model.EventTag
import com.eventrecommender.domain.model.Location
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.data.elasticsearch.core.geo.GeoPoint
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Unit tests for EventDocument persistence mapping.
 * AC: GH-3-AC-5 — Persistence adapter maps domain ↔ ES document without leaking domain models.
 */
class EventDocumentTest {

    private val berlin = Location(52.52, 13.405, "Berlin", "Germany")
    private val now = Instant.now()
    private val futureStart = now.plus(1, ChronoUnit.DAYS)
    private val futureEnd = now.plus(2, ChronoUnit.DAYS)

    private fun aDomainEvent(
        tags: Set<EventTag> = emptySet(),
    ): Event = Event(
        id = EventId.generate(),
        title = "Tech Summit",
        description = "Annual technology summit",
        category = Category.TECHNOLOGY,
        location = berlin,
        venue = "Berlin Congress Center",
        startTime = futureStart,
        endTime = futureEnd,
        tags = tags,
        createdAt = now,
    )

    // AC: GH-3-AC-5 — TEST-P01: fromDomain maps all fields correctly
    @Test
    fun `fromDomain maps all domain fields to EventDocument`() {
        // Given
        val domain = aDomainEvent(tags = setOf(EventTag("kotlin"), EventTag("jvm")))

        // When
        val doc = EventDocument.fromDomain(domain)

        // Then — every field is mapped without loss
        assertEquals(domain.id.value, doc.id)
        assertEquals(domain.title, doc.title)
        assertEquals(domain.description, doc.description)
        assertEquals(domain.category.name, doc.category)
        assertEquals(domain.location.latitude, doc.geoPoint.lat, 0.0001)
        assertEquals(domain.location.longitude, doc.geoPoint.lon, 0.0001)
        assertEquals(domain.location.city, doc.city)
        assertEquals(domain.location.country, doc.country)
        assertEquals(domain.venue, doc.venue)
        assertEquals(domain.startTime, doc.startTime)
        assertEquals(domain.endTime, doc.endTime)
        assertEquals(domain.createdAt, doc.createdAt)
        assertTrue(doc.tags.containsAll(listOf("kotlin", "jvm")))
        assertEquals(2, doc.tags.size)
    }

    // AC: GH-3-AC-5 — TEST-P02: toDomain reconstructs domain event from document
    @Test
    fun `toDomain reconstructs domain event from EventDocument`() {
        // Given
        val doc = EventDocument(
            id = "test-uuid-123",
            title = "Tech Summit",
            description = "Annual technology summit",
            category = "TECHNOLOGY",
            geoPoint = GeoPoint(52.52, 13.405),
            city = "Berlin",
            country = "Germany",
            venue = "Berlin Congress Center",
            startTime = futureStart,
            endTime = futureEnd,
            tags = listOf("kotlin", "spring"),
            createdAt = now,
        )

        // When
        val domain = doc.toDomain()

        // Then
        assertEquals("test-uuid-123", domain.id.value)
        assertEquals("Tech Summit", domain.title)
        assertEquals(Category.TECHNOLOGY, domain.category)
        assertEquals(52.52, domain.location.latitude, 0.0001)
        assertEquals(13.405, domain.location.longitude, 0.0001)
        assertEquals("Berlin", domain.location.city)
        assertEquals("Germany", domain.location.country)
        assertEquals(futureStart, domain.startTime)
        assertEquals(futureEnd, domain.endTime)
        assertEquals(setOf(EventTag("kotlin"), EventTag("spring")), domain.tags)
    }

    // AC: GH-3-AC-5 — TEST-P03: roundtrip fromDomain → toDomain preserves all data
    @Test
    fun `roundtrip fromDomain then toDomain preserves all fields`() {
        // Given
        val original = aDomainEvent(tags = setOf(EventTag("ai"), EventTag("cloud")))

        // When
        val roundTripped = EventDocument.fromDomain(original).toDomain()

        // Then — all fields survive the roundtrip
        assertEquals(original.id, roundTripped.id)
        assertEquals(original.title, roundTripped.title)
        assertEquals(original.description, roundTripped.description)
        assertEquals(original.category, roundTripped.category)
        assertEquals(original.location.latitude, roundTripped.location.latitude, 0.0001)
        assertEquals(original.location.longitude, roundTripped.location.longitude, 0.0001)
        assertEquals(original.location.city, roundTripped.location.city)
        assertEquals(original.location.country, roundTripped.location.country)
        assertEquals(original.venue, roundTripped.venue)
        assertEquals(original.startTime, roundTripped.startTime)
        assertEquals(original.endTime, roundTripped.endTime)
        assertEquals(original.tags, roundTripped.tags)
        assertEquals(original.createdAt, roundTripped.createdAt)
    }

    // AC: GH-3-AC-5 — EP: document with empty tags maps correctly
    @Test
    fun `fromDomain with no tags produces empty tag list`() {
        // Given
        val domain = aDomainEvent(tags = emptySet())

        // When
        val doc = EventDocument.fromDomain(domain)

        // Then
        assertTrue(doc.tags.isEmpty())
    }

    // AC: GH-3-AC-5 — EP: category name survives roundtrip for every category
    @Test
    fun `fromDomain and toDomain correctly map all Category enum values`() {
        Category.entries.forEach { category ->
            val event = Event(
                id = EventId.generate(),
                title = "Test",
                description = "Test event",
                category = category,
                location = berlin,
                venue = "Venue",
                startTime = futureStart,
                endTime = futureEnd,
                tags = emptySet(),
                createdAt = now,
            )
            val roundTripped = EventDocument.fromDomain(event).toDomain()
            assertEquals(category, roundTripped.category) { "Category $category did not survive roundtrip" }
        }
    }
}
