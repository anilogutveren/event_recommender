package com.eventrecommender.domain.model

data class Location(
    val latitude: Double,
    val longitude: Double,
    val city: String,
    val country: String,
) {
    init {
        require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90, got $latitude" }
        require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180, got $longitude" }
        require(city.isNotBlank()) { "City must not be blank" }
        require(country.isNotBlank()) { "Country must not be blank" }
    }

    fun distanceKmTo(other: Location): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(other.latitude - this.latitude)
        val dLon = Math.toRadians(other.longitude - this.longitude)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
        return earthRadiusKm * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    }
}
