package dev.shiron.kotodiscord.util.data.action

import dev.shiron.kotodiscord.metrics.CommandHistory
import dev.shiron.kotodiscord.metrics.MetricsClass
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

data class BotButtonData(
    val event: ButtonInteractionEvent,
    val guild: Guild,
    val actionData: BotActionData,
    val historyData: CommandHistory,
    val metrics: MetricsClass,
) : BotSendEventClass(event, actionData.componentSendType, historyData, metrics)
