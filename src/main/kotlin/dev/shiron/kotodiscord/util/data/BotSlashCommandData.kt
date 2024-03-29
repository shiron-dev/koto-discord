package dev.shiron.kotodiscord.util.data

import dev.shiron.kotodiscord.metrics.CommandHistory
import dev.shiron.kotodiscord.metrics.MetricsClass
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.ItemComponent
import java.util.function.Consumer

data class BotSlashCommandData(
    val event: SlashCommandInteractionEvent,
    val guild: Guild,
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
