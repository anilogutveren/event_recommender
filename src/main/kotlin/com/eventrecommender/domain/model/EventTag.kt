package com.eventrecommender.domain.model

@JvmInline
value class EventTag(val value: String) {
    init {
        require(value.isNotBlank()) { "Tag must not be blank" }
        require(value.length <= 50) { "Tag must not exceed 50 characters" }
    }
}
