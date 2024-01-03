package dev.shiron.kotodiscord.util

import dev.shiron.kotodiscord.metrics.CommandHistory
import dev.shiron.kotodiscord.metrics.MetricsClass
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

data class BotSlashCommandData(
    val event: SlashCommandInteractionEvent,
    val guild: Guild,
    val historyData: CommandHistory,
    val metrics: MetricsClass
) {
    fun reply(message: String) {
        historyData.response = message
        event.hook.sendMessage(message).queue()
        metrics.commandRun(historyData)
    }
}
