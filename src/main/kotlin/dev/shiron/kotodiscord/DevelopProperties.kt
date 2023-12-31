package dev.shiron.kotodiscord

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.dev")
data class DevelopProperties(
    val isDevMode: Boolean = false,
    val devGuildID: String? = null
)
