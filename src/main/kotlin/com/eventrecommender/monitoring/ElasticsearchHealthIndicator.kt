package com.eventrecommender.monitoring

import co.elastic.clients.elasticsearch.ElasticsearchClient
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component

/**
 * Custom Actuator health indicator for Elasticsearch.
 *
 * Reports:
 *  - UP   + cluster name + node count when ES is reachable
 *  - DOWN + error detail when ES is unreachable
 */
@Component("elasticsearch")
class ElasticsearchHealthIndicator(
    private val client: ElasticsearchClient,
) : HealthIndicator {

    override fun health(): Health =
        runCatching {
            val response = client.cluster().health { it }
            Health.up()
                .withDetail("clusterName", response.clusterName())
                .withDetail("status", response.status().jsonValue())
                .withDetail("numberOfNodes", response.numberOfNodes())
                .withDetail("numberOfDataNodes", response.numberOfDataNodes())
                .build()
        }.getOrElse { ex ->
            Health.down()
                .withDetail("error", ex.message ?: "Unknown error")
                .build()
        }
}
