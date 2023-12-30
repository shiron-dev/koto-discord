package dev.shiron.kotodiscord.controller

import dev.shiron.kotodiscord.util.BotSlashCommandData
import dev.shiron.kotodiscord.util.RunnableCommandServiceClass
import dev.shiron.kotodiscord.util.SubCommandsControllerClass
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
    runnableCommandServices: List<RunnableCommandServiceClass>,
    private val subCommandsControllers: List<SubCommandsControllerClass>,
    private val messages: MessageSource
) : ListenerAdapter() {

    private val commandServices: List<RunnableCommandServiceClass> by lazy {
        val commandServices = runnableCommandServices.toMutableList()
        for (commandService in runnableCommandServices) {
            if (subCommandsControllers.find { it.subcommands.contains(commandService) } !== null) {
                commandServices.remove(commandService)
            }
        }
        commandServices
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.subcommandName !== null) {
            // Subcommand
            for (subCommandsController in subCommandsControllers) {
                if (subCommandsController.meta.name == event.name) {
                    subCommandsController.onSlashCommand(
                        BotSlashCommandData(
                            event = event
                        )
                    )
                    return
                }
            }
        } else {
            for (command in commandServices) {
                if (command.meta.name == event.name) {
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
        }
        event.reply(messages.getMessage("command.error.notfound", arrayOf(event.name), Locale.JAPAN)).queue()
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        for (command in commandServices) {
            if (command.meta.name == event.name) {
                command.onAutoComplete(event)
            }
        }
    }

    fun getCommandsData(): List<SlashCommandData> {
        return commandServices.map { it.slashCommandData } + subCommandsControllers.map { it.slashCommandData }
    }
}
