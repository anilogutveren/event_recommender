package com.eventrecommender.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration

@Configuration
class ElasticsearchConfig(
    @Value("\${elasticsearch.host:localhost}") private val host: String,
    @Value("\${elasticsearch.port:9200}") private val port: Int,
) : ElasticsearchConfiguration() {

    override fun clientConfiguration(): ClientConfiguration =
        ClientConfiguration.builder()
            .connectedTo("$host:$port")
            .build()
}
