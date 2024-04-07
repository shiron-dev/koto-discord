package dev.shiron.kotodiscord.util.data.action

import dev.shiron.kotodiscord.metrics.CommandHistory
import dev.shiron.kotodiscord.metrics.MetricsClass
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.requests.RestAction
import java.util.function.Consumer

abstract class BotSendEventClass(
    private val interactionEvent: IDeferrableCallback,
    private val componentSendType: ComponentSendType,
    private val historyData: CommandHistory,
    private val metrics: MetricsClass,
) {
    fun send(
        message: String,
        actionComponents: List<ItemComponent>? = null,
        success: Consumer<in Message>? = null,
    ) {
        historyData.response = message

        val msgObj: RestAction<Message>? =
            run {
                when (componentSendType) {
                    ComponentSendType.DEFER -> {
                        val channel = interactionEvent.channel?.let { interactionEvent.guild?.getTextChannelById(it.idLong) }
                        return@run channel?.sendMessage(message)
                    }

                    ComponentSendType.REPLAY -> {
                        var msgObj = interactionEvent.hook.sendMessage(message)
                        if (actionComponents != null) {
                            msgObj = msgObj.addActionRow(actionComponents)
                        }
                        return@run msgObj
                    }

                    ComponentSendType.EDIT -> {
                        var msgObj = interactionEvent.hook.editOriginal(message)
                        msgObj =
                            if (actionComponents == null) {
                                msgObj.setComponents()
                            } else {
                                msgObj.setActionRow(actionComponents)
                            }
                        return@run msgObj
                    }
                }
            }

        msgObj?.queue(success)
        metrics.commandRun(historyData)
    }
}
