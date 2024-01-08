package dev.shiron.kotodiscord.util.data

import dev.shiron.kotodiscord.metrics.CommandHistory
import dev.shiron.kotodiscord.metrics.MetricsClass
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import java.util.function.Consumer

data class BotStringSelectData(
    val event: StringSelectInteractionEvent,
    val values: List<String>,
    val guild: Guild,
    val actionData: BotActionData,
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
