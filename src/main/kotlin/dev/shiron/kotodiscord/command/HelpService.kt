package dev.shiron.kotodiscord.command

import dev.shiron.kotodiscord.i18n.I18n
import dev.shiron.kotodiscord.util.data.action.BotSlashCommandData
import dev.shiron.kotodiscord.util.meta.SingleCommandEnum
import dev.shiron.kotodiscord.util.meta.SubCommandEnum
import dev.shiron.kotodiscord.util.service.SingleCommandServiceClass
import dev.shiron.kotodiscord.vars.properties.AppProperties
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.components.buttons.Button
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class HelpService
    @Autowired
    constructor(
        private val appProperties: AppProperties,
        private val i18n: I18n,
    ) : SingleCommandServiceClass(
            SingleCommandEnum.HELP,
            i18n,
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
                        i18n.format("command.option.command"),
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
                            i18n.format("button.support"),
                        ),
                    ).queue()
            }

            if (command != null) {
                if (commands.contains(command)) {
                    reply("### $command\n${getHelp(command)}")
                } else {
                    reply(
                        i18n.format("command.message.help.notfound", command),
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
            return i18n.format("command.help.$cmdStr")
        }
    }
