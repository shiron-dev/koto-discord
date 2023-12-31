package dev.shiron.kotodiscord.metrics

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import java.time.LocalDateTime

data class CommandHistory(
    val commandName: String,
    val eventId: String,
    val guildId: String?,
    val channelId: String,
    val userId: String,
    val options: Map<String, String>,
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    val timestamp: LocalDateTime,
    var response: String
)
