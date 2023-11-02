package dev.shiron.kotodiscord.util

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

data class BotSlashCommandData(
    val event: SlashCommandInteractionEvent
) {
    fun reply(message: String) {
        event.hook.sendMessage(message).queue()
    }
}
