package dev.shiron.kotodiscord

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.notification")
data class NotificationProperties(
    var guildID: String? = null,
    var channelID: String? = null,
    var isStartMessage: Boolean = false,
)
