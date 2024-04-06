package dev.shiron.kotodiscord.util.data.action

import dev.shiron.kotodiscord.metrics.CommandHistory
import dev.shiron.kotodiscord.metrics.MetricsClass
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

data class BotSlashCommandData(
    val event: SlashCommandInteractionEvent,
    val guild: Guild,
    val shared: Boolean,
    val historyData: CommandHistory,
    val metrics: MetricsClass,
) : BotReplayEventClass(event, historyData, metrics)
