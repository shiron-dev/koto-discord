package dev.shiron.kotodiscord.vars.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    var token: String? = null,
    var activityMessage: String? = null,
    var metricsDir: String? = null,
    var actionDataCleanMin: Int? = null,
    var inviteLink: String? = null,
)
