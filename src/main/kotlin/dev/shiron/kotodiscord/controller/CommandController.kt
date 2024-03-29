package dev.shiron.kotodiscord.controller

import dev.shiron.kotodiscord.AppProperties
import dev.shiron.kotodiscord.metrics.CommandHistory
import dev.shiron.kotodiscord.metrics.MetricsClass
import dev.shiron.kotodiscord.util.ActionDataManager
import dev.shiron.kotodiscord.util.RunnableCommandServiceClass
import dev.shiron.kotodiscord.util.SingleCommandServiceClass
import dev.shiron.kotodiscord.util.SubCommandServiceClass
import dev.shiron.kotodiscord.util.data.BotSlashCommandData
import dev.shiron.kotodiscord.util.data.BotStringSelectData
import dev.shiron.kotodiscord.util.data.ComponentIdData
import dev.shiron.kotodiscord.util.meta.SubCommandGroupEnum
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Controller
import java.time.LocalDateTime
import java.util.*

@Controller
class CommandController
    @Autowired
    constructor(
        private val singleCommandServices: List<SingleCommandServiceClass>,
        private val subCommandServices: List<SubCommandServiceClass>,
        private val appProperties: AppProperties,
        private val messages: MessageSource,
        private val metrics: MetricsClass,
    ) : ListenerAdapter() {
        override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
            val command = getCommand(event.name, event.subcommandName)
            val guild = event.guild
            if (command != null && guild != null) {
                event
                    .deferReply()
                    .setEphemeral(
                        (event.getOption("shared")?.asBoolean?.not())
                            ?: command.sharedDefault.not(),
                    )
                    .queue()
                command.onSlashCommand(
                    BotSlashCommandData(
                        event = event,
                        guild = guild,
                        historyData =
                            CommandHistory(
                                commandName = "${event.name}.${event.subcommandName}",
                                eventId = event.id,
                                guildId = event.guild?.id,
                                channelId = event.channel.id,
                                userId = event.user.id,
                                options = event.options.associate { it.name to it.asString },
                                isAction = false,
                                timestamp = LocalDateTime.now(),
                                response = "",
                            ),
                        metrics = metrics,
                    ),
                )
            } else {
                event.reply(
                    messages.getMessage(
                        "command.error.notfound",
                        arrayOf(event.name),
                        Locale.JAPAN,
                    ),
                ).queue()
            }
        }

        override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
            val command = getCommand(event.name, event.subcommandName)
            command?.onAutoComplete(event)
        }

        override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
            val guild = event.guild
            val actionData = ActionDataManager[event.componentId]
            if (actionData == null) {
                event.reply(messages.getMessage("command.error.action", arrayOf(), Locale.JAPAN)).setEphemeral(true).queue()
                return
            }
            val command = getCommandFromComponentId(actionData.componentIdData)
            if (guild == null || command == null) {
                event.reply(messages.getMessage("command.error.internal", arrayOf(), Locale.JAPAN)).queue()
                return
            }

            ActionDataManager.removeActionData(event.componentId)

            val data =
                BotStringSelectData(
                    event = event,
                    values = event.values,
                    guild = guild,
                    actionData = actionData,
                    historyData =
                        CommandHistory(
                            commandName = actionData.componentIdData.componentId,
                            eventId = event.id,
                            guildId = event.guild?.id,
                            channelId = event.channel.id,
                            userId = event.user.id,
                            options = mapOf(),
                            isAction = true,
                            timestamp = LocalDateTime.now(),
                            response = "",
                        ),
                    metrics = metrics,
                )

            event.deferReply().setEphemeral(actionData.isShow).queue()

            command.onStringSelect(data)
        }

        fun getCommand(
            name: String,
            subcommandName: String?,
        ): RunnableCommandServiceClass? {
            if (subcommandName !== null) {
                // Subcommand
                for (command in subCommandServices) {
                    if (command.commandMeta.metadata.commandName == subcommandName && command.commandMeta.group.metadata.commandName == name) {
                        return command
                    }
                }
            } else {
                // Single command
                for (command in singleCommandServices) {
                    if (command.commandMeta.metadata.commandName == name) {
                        return command
                    }
                }
            }
            return null
        }

        fun getCommandFromComponentId(componentIdData: ComponentIdData): RunnableCommandServiceClass? {
            for (command in singleCommandServices) {
                if (command.commandMeta.metadata.commandName == componentIdData.commandName) {
                    return command
                }
            }
            for (command in subCommandServices) {
                if ("${command.commandMeta.group.metadata.commandName}.${command.commandMeta.metadata.commandName}" == componentIdData.commandName) {
                    return command
                }
            }
            return null
        }

        @Scheduled(fixedDelay = 30)
        private fun clearActionData() {
            ActionDataManager.cleanByMin(appProperties.actionDataCleanMin ?: 60)
        }

        fun getCommandsData(): List<SlashCommandData> {
            val subcommands =
                mutableMapOf<SubCommandGroupEnum, MutableList<SubCommandServiceClass>>()
            for (command in subCommandServices) {
                if (!subcommands.containsKey(command.commandMeta.group)) {
                    subcommands[command.commandMeta.group] = mutableListOf()
                }
                subcommands[command.commandMeta.group]?.add(command)
            }
            val subCommandsData =
                subcommands.map {
                    Commands.slash(
                        it.key.metadata.commandName,
                        messages.getMessage(
                            "command.description.${it.key.metadata.commandName}",
                            arrayOf(),
                            Locale.JAPAN,
                        ),
                    ).addSubcommands(it.value.map { it1 -> it1.subcommandData })
                }
            return singleCommandServices.map { it.slashCommandData } + subCommandsData
        }
    }
