package dev.shiron.kotodiscord.util.data.action

import dev.shiron.kotodiscord.metrics.CommandHistory
import dev.shiron.kotodiscord.metrics.MetricsClass
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback
import net.dv8tion.jda.api.interactions.components.ItemComponent
import java.util.function.Consumer

abstract class BotReplayEventClass(
    private val interactionEvent: IDeferrableCallback,
    private val historyData: CommandHistory,
    private val metrics: MetricsClass,
) {
    fun reply(
        message: String,
        actionComponents: List<ItemComponent>? = null,
        success: Consumer<in Message>? = null,
    ) {
        historyData.response = message
        var msgObj = interactionEvent.hook.sendMessage(message)

        if (actionComponents != null) {
            msgObj = msgObj.addActionRow(actionComponents)
        }

        msgObj.queue(success)
        metrics.commandRun(historyData)
    }

    fun edit(
        message: String,
        actionComponents: List<ItemComponent>? = null,
        success: Consumer<in Message>? = null,
    ) {
        historyData.response = message
        var msgObj = interactionEvent.hook.editOriginal(message)

        if (actionComponents == null) {
            msgObj = msgObj.setComponents()
        } else {
            msgObj = msgObj.setActionRow(actionComponents)
        }

        msgObj.queue(success)
        metrics.commandRun(historyData)
    }
}
