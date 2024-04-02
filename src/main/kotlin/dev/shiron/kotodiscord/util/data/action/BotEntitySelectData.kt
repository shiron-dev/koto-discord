package dev.shiron.kotodiscord.util.data.action

import dev.shiron.kotodiscord.metrics.CommandHistory
import dev.shiron.kotodiscord.metrics.MetricsClass
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent

data class BotEntitySelectData(
    val event: EntitySelectInteractionEvent,
    val values: List<IMentionable>,
    val guild: Guild,
    val actionData: BotActionData,
    val historyData: CommandHistory,
    val metrics: MetricsClass,
) : BotReplayEventClass(event, historyData, metrics)
