package dev.shiron.kotodiscord

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    var token: String? = null,
    var activityMessage: String? = null,
    var metricsDir: String? = null,
)
