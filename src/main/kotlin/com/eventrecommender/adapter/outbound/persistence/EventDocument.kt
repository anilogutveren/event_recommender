package com.eventrecommender.adapter.outbound.persistence

import com.eventrecommender.domain.model.Category
import com.eventrecommender.domain.model.Event
import com.eventrecommender.domain.model.EventId
import com.eventrecommender.domain.model.EventTag
import com.eventrecommender.domain.model.Location
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.GeoPointField
import org.springframework.data.elasticsearch.core.geo.GeoPoint
import java.time.Instant

/**
 * EventDocument — Elasticsearch persistence model.
 *
 * Lives exclusively in the adapter layer.
 * Provides mapping methods to/from the domain Event model.
 */
@Document(indexName = "events", createIndex = false)
data class EventDocument(
    @Id
    val id: String,

    @Field(type = FieldType.Text)
    val title: String,

    @Field(type = FieldType.Text)
    val description: String,

    @Field(type = FieldType.Keyword)
    val category: String,

    @GeoPointField
    val geoPoint: GeoPoint,

    @Field(type = FieldType.Keyword)
    val city: String,

    @Field(type = FieldType.Keyword)
    val country: String,

    @Field(type = FieldType.Keyword)
    val venue: String,

    @Field(type = FieldType.Date)
    val startTime: Instant,

    @Field(type = FieldType.Date)
    val endTime: Instant,

    @Field(type = FieldType.Keyword)
    val tags: List<String>,

    @Field(type = FieldType.Date)
    val createdAt: Instant,
) {
    companion object {
        fun fromDomain(event: Event): EventDocument = EventDocument(
            id = event.id.value,
            title = event.title,
            description = event.description,
            category = event.category.name,
            geoPoint = GeoPoint(event.location.latitude, event.location.longitude),
            city = event.location.city,
            country = event.location.country,
            venue = event.venue,
            startTime = event.startTime,
            endTime = event.endTime,
            tags = event.tags.map { it.value },
            createdAt = event.createdAt,
        )
    }

    fun toDomain(): Event = Event(
        id = EventId.of(id),
        title = title,
        description = description,
        category = Category.valueOf(category),
        location = Location(
            latitude = geoPoint.lat,
            longitude = geoPoint.lon,
            city = city,
            country = country,
        ),
        venue = venue,
        startTime = startTime,
        endTime = endTime,
        tags = tags.map { EventTag(it) }.toSet(),
        createdAt = createdAt,
    )
}
