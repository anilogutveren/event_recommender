package com.eventrecommender.monitoring

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.testcontainers.elasticsearch.ElasticsearchContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ElasticsearchMonitoringIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    companion object {
        @Container
        val elasticsearch = ElasticsearchContainer(
            "docker.elastic.co/elasticsearch/elasticsearch:8.17.0"
        ).apply {
            withEnv("xpack.security.enabled", "false")
        }

        @JvmStatic
        @DynamicPropertySource
        fun elasticsearchProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.elasticsearch.uris") {
                "http://${elasticsearch.host}:${elasticsearch.firstMappedPort}"
            }
        }
    }

    @Test
    fun `health endpoint reports elasticsearch UP when cluster is reachable`() {
        mockMvc.get("/actuator/health")
            .andExpect {
                status { isOk() }
                jsonPath("$.components.elasticsearch.status") { value("UP") }
                jsonPath("$.components.elasticsearch.details.clusterName") { isString() }
            }
    }

    @Test
    fun `prometheus endpoint exposes es query duration metric`() {
        mockMvc.get("/actuator/prometheus")
            .andExpect {
                status { isOk() }
                content {
                    string(org.hamcrest.Matchers.containsString("es_query_duration"))
                }
            }
    }

    @Test
    fun `prometheus endpoint exposes es index operations counter`() {
        mockMvc.get("/actuator/prometheus")
            .andExpect {
                status { isOk() }
                content {
                    string(org.hamcrest.Matchers.containsString("es_index_operations"))
                }
            }
    }
}
