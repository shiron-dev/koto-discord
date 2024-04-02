package dev.shiron.kotodiscord.service.command

import dev.shiron.kotodiscord.AppProperties
import dev.shiron.kotodiscord.util.SingleCommandServiceClass
import dev.shiron.kotodiscord.util.data.action.BotSlashCommandData
import dev.shiron.kotodiscord.util.meta.SingleCommandEnum
import dev.shiron.kotodiscord.util.meta.SubCommandEnum
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.components.buttons.Button
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*

@Service
class HelpService
    @Autowired
    constructor(
        private val appProperties: AppProperties,
        private val messages: MessageSource,
    ) : SingleCommandServiceClass(
            SingleCommandEnum.HELP,
            messages,
        ) {
        val commands =
            SingleCommandEnum.values().map { it.metadata.commandName } +
                SubCommandEnum.values().map { "${it.group.metadata.commandName} ${it.metadata.commandName}" }

        override val commandOptions: List<OptionData>
            get() =
                listOf(
                    OptionData(
                        OptionType.STRING,
                        "command",
                        messages.getMessage(
                            "command.option.command",
                            arrayOf(),
                            Locale.JAPAN,
                        ),
                    ).setAutoComplete(true),
                )

        override fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {
            when (event.focusedOption.name) {
                "command" -> {
                    event.replyChoiceStrings(
                        commands.filter {
                            it.startsWith(event.focusedOption.value.lowercase()) ||
                                it.split(" ").getOrNull(1)?.startsWith(
                                    event.focusedOption.value.lowercase(),
                                ) == true
                        },
                    ).queue()
                }
            }
        }

        override fun onSlashCommand(cmd: BotSlashCommandData) {
            val command = cmd.event.getOption("command")?.asString?.lowercase()

            fun reply(message: String) {
                cmd.event.hook.sendMessage(message)
                    .addActionRow(
                        Button.link(
                            appProperties.inviteLink ?: "",
                            messages.getMessage("button.support", arrayOf(), Locale.JAPAN),
                        ),
                    ).queue()
            }

            if (command != null) {
                if (commands.contains(command)) {
                    reply("### $command\n${getHelp(command)}")
                } else {
                    reply(
                        messages.getMessage(
                            "command.message.help.notfound",
                            arrayOf(command),
                            Locale.JAPAN,
                        ),
                    )
                }
            } else {
                reply(
                    commands.joinToString(
                        "\n",
                    ) { "### $it\n ${getHelp(it)}" },
                )
            }
        }

        private fun getHelp(command: String): String {
            val cmdStr = command.replace(" ", ".")
            return messages.getMessage(
                "command.help.$cmdStr",
                arrayOf(),
                null,
                Locale.JAPAN,
            )
                ?: messages.getMessage("command.description.$cmdStr", arrayOf(), Locale.JAPAN)
        }
    }
