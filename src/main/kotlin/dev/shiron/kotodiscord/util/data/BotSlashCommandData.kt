package dev.shiron.kotodiscord.util.data

import dev.shiron.kotodiscord.metrics.CommandHistory
import dev.shiron.kotodiscord.metrics.MetricsClass
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.util.function.Consumer

data class BotSlashCommandData(
    val event: SlashCommandInteractionEvent,
    val guild: Guild,
    val historyData: CommandHistory,
    val metrics: MetricsClass,
) {
    fun reply(
        message: String,
        success: Consumer<in Message>? = null,
    ) {
        historyData.response = message
        event.hook.sendMessage(message).queue(success)
        metrics.commandRun(historyData)
    }
}
