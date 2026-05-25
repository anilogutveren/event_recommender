package com.eventrecommender.monitoring

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
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
    private val elasticsearchOperations: ElasticsearchOperations,
) : HealthIndicator {

    override fun health(): Health =
        runCatching {
            val clusterHealth = elasticsearchOperations.cluster().health()
            Health.up()
                .withDetail("clusterName", clusterHealth.clusterName)
                .withDetail("status", clusterHealth.status.name)
                .withDetail("numberOfNodes", clusterHealth.numberOfNodes)
                .withDetail("numberOfDataNodes", clusterHealth.numberOfDataNodes)
                .build()
        }.getOrElse { ex ->
            Health.down()
                .withDetail("error", ex.message ?: "Unknown error")
                .build()
        }
}
