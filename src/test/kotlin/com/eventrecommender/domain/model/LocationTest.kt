package com.eventrecommender.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.math.abs

class LocationTest {

    // AC: GH-3-AC-2 — Domain model: valid location can be created
    @Test
    fun `valid location is created`() {
        val loc = Location(52.52, 13.405, "Berlin", "Germany")
        assertEquals(52.52, loc.latitude)
    }

    // AC: GH-3-AC-2 — TEST-L01: BVA — latitude just above 90 is invalid
    @Test
    fun `latitude above 90 throws`() {
        assertThrows<IllegalArgumentException> { Location(91.0, 13.405, "Berlin", "Germany") }
    }

    // AC: GH-3-AC-2 — TEST-L02: BVA — latitude just below -90 is invalid
    @Test
    fun `latitude below minus 90 throws`() {
        assertThrows<IllegalArgumentException> { Location(-91.0, 13.405, "Berlin", "Germany") }
    }

    // AC: GH-3-AC-2 — TEST-L03: BVA — longitude just above 180 is invalid
    @Test
    fun `longitude above 180 throws`() {
        assertThrows<IllegalArgumentException> { Location(52.52, 181.0, "Berlin", "Germany") }
    }

    // AC: GH-3-AC-2 — TEST-L04: BVA — longitude just below -180 is invalid
    @Test
    fun `longitude below minus 180 throws`() {
        assertThrows<IllegalArgumentException> { Location(52.52, -181.0, "Berlin", "Germany") }
    }

    // AC: GH-3-AC-2 — TEST-L05: BVA — latitude at boundary 90.0 is valid
    @Test
    fun `latitude at boundary 90 is valid`() {
        val loc = Location(90.0, 0.0, "North Pole", "Arctic")
        assertEquals(90.0, loc.latitude)
    }

    // AC: GH-3-AC-2 — TEST-L06: BVA — latitude at boundary -90.0 is valid
    @Test
    fun `latitude at boundary minus 90 is valid`() {
        val loc = Location(-90.0, 0.0, "South Pole", "Antarctic")
        assertEquals(-90.0, loc.latitude)
    }

    // AC: GH-3-AC-2 — Domain model: blank city is not allowed
    @Test
    fun `blank city throws`() {
        assertThrows<IllegalArgumentException> { Location(52.52, 13.405, " ", "Germany") }
    }

    // AC: GH-3-AC-2 — Domain model: blank country is not allowed
    @Test
    fun `blank country throws`() {
        assertThrows<IllegalArgumentException> { Location(52.52, 13.405, "Berlin", "  ") }
    }

    // AC: GH-3-AC-2 — TEST-L07: distance to itself is zero
    @Test
    fun `distance between same location is zero`() {
        val berlin = Location(52.52, 13.405, "Berlin", "Germany")
        assertTrue(abs(berlin.distanceKmTo(berlin)) < 0.01)
    }

    // AC: GH-3-AC-2 — TEST-L08: known distance between real cities
    @Test
    fun `distance between Berlin and Munich is approximately 504 km`() {
        val berlin = Location(52.52, 13.405, "Berlin", "Germany")
        val munich = Location(48.137, 11.576, "Munich", "Germany")
        val dist = berlin.distanceKmTo(munich)
        assertTrue(dist in 490.0..520.0) { "Expected ~504 km, got $dist" }
    }

    // AC: GH-3-AC-2 — distance is symmetric
    @Test
    fun `distance is symmetric`() {
        val berlin = Location(52.52, 13.405, "Berlin", "Germany")
        val munich = Location(48.137, 11.576, "Munich", "Germany")
        val diff = abs(berlin.distanceKmTo(munich) - munich.distanceKmTo(berlin))
        assertTrue(diff < 0.001) { "Distance should be symmetric" }
    }
}
