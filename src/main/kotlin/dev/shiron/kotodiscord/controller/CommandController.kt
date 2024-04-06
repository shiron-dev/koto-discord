package dev.shiron.kotodiscord.controller

import dev.shiron.kotodiscord.i18n.I18n
import dev.shiron.kotodiscord.metrics.CommandHistory
import dev.shiron.kotodiscord.metrics.MetricsClass
import dev.shiron.kotodiscord.util.data.action.*
import dev.shiron.kotodiscord.util.meta.SubCommandGroupEnum
import dev.shiron.kotodiscord.util.service.RunnableCommandServiceClass
import dev.shiron.kotodiscord.util.service.SingleCommandServiceClass
import dev.shiron.kotodiscord.util.service.SubCommandServiceClass
import dev.shiron.kotodiscord.vars.properties.AppProperties
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Controller
import java.time.LocalDateTime

@Controller
class CommandController
    @Autowired
    constructor(
        private val singleCommandServices: List<SingleCommandServiceClass>,
        private val subCommandServices: List<SubCommandServiceClass>,
        private val appProperties: AppProperties,
        private val i18n: I18n,
        private val metrics: MetricsClass,
    ) : ListenerAdapter() {
        override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
            val command = getCommand(event.name, event.subcommandName)
            val guild = event.guild
            if (command != null && guild != null) {
                val shared =
                    (event.getOption("shared")?.asBoolean?.not())
                        ?: command.sharedDefault.not()
                event
                    .deferReply()
                    .setEphemeral(shared)
                    .queue()
                command.onSlashCommand(
                    BotSlashCommandData(
                        event = event,
                        guild = guild,
                        shared = shared,
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
                    i18n.format(
                        "command.error.notfound",
                        event.name,
                    ),
                ).queue()
            }
        }

        override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
            val command = getCommand(event.name, event.subcommandName)
            command?.onAutoComplete(event)
        }

        override fun onButtonInteraction(event: ButtonInteractionEvent) {
            val guild = event.guild
            val actionData = ActionDataManager[event.componentId]
            if (actionData == null) {
                event.reply(i18n.format("command.error.action")).setEphemeral(true).queue()
                return
            }
            val command = getCommandFromComponentId(actionData.componentIdData)
            if (guild == null || command == null) {
                event.reply(i18n.format("command.error.internal")).queue()
                return
            }

            ActionDataManager.removeActionData(event.componentId)

            val data =
                BotButtonData(
                    event = event,
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

            when (actionData.componentReplayType) {
                ComponentReplayType.REPLAY -> event.deferReply().setEphemeral(actionData.isShow).queue()
                ComponentReplayType.EDIT -> event.deferEdit().queue()
                else -> {}
            }

            command.onButton(data)
        }

        override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
            val guild = event.guild
            val actionData = ActionDataManager[event.componentId]
            if (actionData == null) {
                event.reply(i18n.format("command.error.action")).setEphemeral(true).queue()
                return
            }
            val command = getCommandFromComponentId(actionData.componentIdData)
            if (guild == null || command == null) {
                event.reply(i18n.format("command.error.internal")).queue()
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

            when (actionData.componentReplayType) {
                ComponentReplayType.REPLAY -> event.deferReply().setEphemeral(actionData.isShow).queue()
                ComponentReplayType.EDIT -> event.deferEdit().queue()
                else -> {}
            }

            command.onStringSelect(data)
        }

        override fun onEntitySelectInteraction(event: EntitySelectInteractionEvent) {
            val guild = event.guild
            val actionData = ActionDataManager[event.componentId]
            if (actionData == null) {
                event.reply(i18n.format("command.error.action")).setEphemeral(true).queue()
                return
            }
            val command = getCommandFromComponentId(actionData.componentIdData)
            if (guild == null || command == null) {
                event.reply(i18n.format("command.error.internal")).queue()
                return
            }

            ActionDataManager.removeActionData(event.componentId)

            val data =
                BotEntitySelectData(
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

            when (actionData.componentReplayType) {
                ComponentReplayType.REPLAY -> event.deferReply().setEphemeral(actionData.isShow).queue()
                ComponentReplayType.EDIT -> event.deferEdit().queue()
                else -> {}
            }

            command.onEntitySelect(data)
        }

        fun getCommand(
            name: String,
            subcommandName: String?,
        ): RunnableCommandServiceClass? {
            if (subcommandName != null) {
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
                        i18n.format("command.description.${it.key.metadata.commandName}"),
                    ).addSubcommands(it.value.map { it1 -> it1.subcommandData })
                }
            return singleCommandServices.map { it.slashCommandData } + subCommandsData
        }
    }
