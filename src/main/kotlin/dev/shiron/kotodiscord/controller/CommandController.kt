package dev.shiron.kotodiscord.controller

import dev.shiron.kotodiscord.util.BotSlashCommandData
import dev.shiron.kotodiscord.util.CommandServiceClass
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Controller
import java.util.*

@Controller
class CommandController @Autowired constructor(
    private val commandServices: List<CommandServiceClass>,
    private val messages: MessageSource
) : ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        for (command in commandServices) {
            if (command.meta.name.lowercase() == event.name.lowercase()) {
                event
                    .deferReply()
                    .setEphemeral((event.getOption("shared")?.asBoolean?.not()) ?: command.sharedDefault.not())
                    .queue()
                command.onSlashCommand(
                    BotSlashCommandData(
                        event = event
                    )
                )
                return
            }
        }
        event.reply(messages.getMessage("command.error.notfound", arrayOf(event.name), Locale.JAPAN)).queue()
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        for (command in commandServices) {
            if (command.meta.name.lowercase() == event.name.lowercase()) {
                command.onAutoComplete(event)
            }
        }
    }

    fun getCommandsData(): List<SlashCommandData> {
        return commandServices.map { it.slashCommandData }
    }
}
