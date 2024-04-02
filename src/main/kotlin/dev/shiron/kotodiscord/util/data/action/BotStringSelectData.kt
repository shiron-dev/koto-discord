package dev.shiron.kotodiscord.util.data.action

import dev.shiron.kotodiscord.metrics.CommandHistory
import dev.shiron.kotodiscord.metrics.MetricsClass
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent

data class BotStringSelectData(
    val event: StringSelectInteractionEvent,
    val values: List<String>,
    val guild: Guild,
    val actionData: BotActionData,
    val historyData: CommandHistory,
    val metrics: MetricsClass,
) : BotReplayEventClass(event, historyData, metrics)
