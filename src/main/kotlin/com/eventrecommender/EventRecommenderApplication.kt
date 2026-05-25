package com.eventrecommender

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EventRecommenderApplication

fun main(args: Array<String>) {
    runApplication<EventRecommenderApplication>(*args)
}
