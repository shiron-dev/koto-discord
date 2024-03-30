package dev.shiron.kotodiscord.util.data

import dev.shiron.kotodiscord.metrics.CommandHistory
import dev.shiron.kotodiscord.metrics.MetricsClass
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent
import net.dv8tion.jda.api.interactions.components.ItemComponent
import java.util.function.Consumer

data class BotEntitySelectData(
    val event: EntitySelectInteractionEvent,
    val values: List<IMentionable>,
    val guild: Guild,
    val actionData: BotActionData,
    val historyData: CommandHistory,
    val metrics: MetricsClass,
) {
    fun reply(
        message: String,
        actionComponents: List<ItemComponent>? = null,
        success: Consumer<in Message>? = null,
    ) {
        historyData.response = message
        var msgObj = event.hook.sendMessage(message)

        if (actionComponents != null) {
            msgObj = msgObj.addActionRow(actionComponents)
        }

        msgObj.queue(success)
        metrics.commandRun(historyData)
    }
}
