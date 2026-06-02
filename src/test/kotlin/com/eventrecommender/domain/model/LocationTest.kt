package com.eventrecommender.domain.model

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.math.abs

class LocationTest {

    @Test
    fun `valid location is created`() {
        val loc = Location(52.52, 13.405, "Berlin", "Germany")
        assertTrue(loc.latitude == 52.52)
    }

    @Test
    fun `invalid latitude throws`() {
        assertThrows<IllegalArgumentException> { Location(91.0, 13.405, "Berlin", "Germany") }
    }

    @Test
    fun `invalid longitude throws`() {
        assertThrows<IllegalArgumentException> { Location(52.52, 181.0, "Berlin", "Germany") }
    }

    @Test
    fun `blank city throws`() {
        assertThrows<IllegalArgumentException> { Location(52.52, 13.405, " ", "Germany") }
    }

    @Test
    fun `distance between same location is zero`() {
        val berlin = Location(52.52, 13.405, "Berlin", "Germany")
        assertTrue(abs(berlin.distanceKmTo(berlin)) < 0.01)
    }

    @Test
    fun `distance between Berlin and Munich is approximately 504 km`() {
        val berlin = Location(52.52, 13.405, "Berlin", "Germany")
        val munich = Location(48.137, 11.576, "Munich", "Germany")
        val dist = berlin.distanceKmTo(munich)
        assertTrue(dist in 490.0..520.0) { "Expected ~504 km, got $dist" }
    }
}
