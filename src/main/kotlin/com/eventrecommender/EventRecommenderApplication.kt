package com.eventrecommender

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class EventRecommenderApplication

fun main(args: Array<String>) {
    runApplication<EventRecommenderApplication>(*args)
}
