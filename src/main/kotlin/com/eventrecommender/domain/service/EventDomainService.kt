package com.eventrecommender.domain.service

import com.eventrecommender.domain.model.Category
import com.eventrecommender.domain.model.Event
import com.eventrecommender.domain.model.Location

/**
 * EventDomainService — pure domain logic that does not belong to a single entity.
 *
 * No Spring, no infrastructure dependencies.
 */
class EventDomainService {

    /**
     * Filter events by category, optional location radius, and optional date range.
     * Strategy pattern: filtering logic is encapsulated here and can be varied independently.
     */
    fun filterByPreferences(
        events: List<Event>,
        categories: Set<Category>,
        userLocation: Location?,
        maxDistanceKm: Double?,
    ): List<Event> {
        return events
            .filter { event -> categories.isEmpty() || event.category in categories }
            .filter { event ->
                if (userLocation != null && maxDistanceKm != null) {
                    event.location.distanceKmTo(userLocation) <= maxDistanceKm
                } else {
                    true
                }
            }
    }

    /**
     * Rank events by relevance: upcoming events score higher, geo-proximity boosts score.
     */
    fun rank(events: List<Event>, userLocation: Location?): List<Event> {
        return events.sortedWith(
            compareBy(
                { if (it.isUpcoming()) 0 else 1 },
                { userLocation?.distanceKmTo(it.location) ?: 0.0 },
            ),
        )
    }
}
