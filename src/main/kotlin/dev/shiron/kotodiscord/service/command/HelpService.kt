package dev.shiron.kotodiscord.service.command

import dev.shiron.kotodiscord.util.BotSlashCommandData
import dev.shiron.kotodiscord.util.SingleCommandServiceClass
import dev.shiron.kotodiscord.util.meta.SingleCommandEnum
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*

@Service
class HelpService
    @Autowired
    constructor(
        private val messages: MessageSource,
    ) : SingleCommandServiceClass(
            SingleCommandEnum.HELP,
            messages,
        ) {
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
                    val commands = SingleCommandEnum.values().map { it.metadata.commandName }
                    event.replyChoiceStrings(
                        commands.filter {
                            it.startsWith(event.focusedOption.value.lowercase())
                        },
                    ).queue()
                }
            }
        }

        override fun onSlashCommand(cmd: BotSlashCommandData) {
            val command = cmd.event.getOption("command")?.asString?.lowercase()
            val commands = SingleCommandEnum.values().map { it.metadata.commandName }
            if (command != null) {
                if (commands.contains(command)) {
                    cmd.reply("### $command\n${getHelp(command)}")
                } else {
                    cmd.reply(
                        messages.getMessage(
                            "command.message.help.notfound",
                            arrayOf(command),
                            Locale.JAPAN,
                        ),
                    )
                }
            } else {
                cmd.reply(
                    commands.joinToString(
                        "\n\n",
                    ) { "### $it\n ${getHelp(it)}" },
                )
            }
        }

        private fun getHelp(command: String): String {
            return messages.getMessage(
                "command.help.$command",
                arrayOf(),
                null,
                Locale.JAPAN,
            )
                ?: messages.getMessage("command.description.$command", arrayOf(), Locale.JAPAN)
        }
    }
