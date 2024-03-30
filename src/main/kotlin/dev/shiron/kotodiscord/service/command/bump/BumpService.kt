package dev.shiron.kotodiscord.service.command.bump

import dev.shiron.kotodiscord.domain.BumpConfigData
import dev.shiron.kotodiscord.repository.BumpConfigDataRepository
import dev.shiron.kotodiscord.util.SingleCommandServiceClass
import dev.shiron.kotodiscord.util.data.BotButtonData
import dev.shiron.kotodiscord.util.data.BotEntitySelectData
import dev.shiron.kotodiscord.util.data.BotSlashCommandData
import dev.shiron.kotodiscord.util.data.ComponentReplayType
import dev.shiron.kotodiscord.util.meta.SingleCommandEnum
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*

@Service
class BumpService
    @Autowired
    constructor(
        private val configRepository: BumpConfigDataRepository,
        private val messages: MessageSource,
    ) : SingleCommandServiceClass(
            SingleCommandEnum.BUMP,
            messages,
        ) {
        override fun onSlashCommand(cmd: BotSlashCommandData) {
            val config = configRepository.findByGuildId(cmd.guild.idLong)

            if (config == null) {
                cmd.reply(
                    messages.getMessage(
                        "command.message.bump.none",
                        null,
                        Locale.JAPAN,
                    ),
                    listOf(
                        Button.secondary(
                            getComponentId("set", ComponentReplayType.EDIT),
                            messages.getMessage(
                                "button.bump.set",
                                null,
                                Locale.JAPAN,
                            ),
                        ),
                    ),
                )
            } else {
                cmd.reply(
                    messages.getMessage(
                        "command.message.bump.msg",
                        arrayOf(
                            "<#${config.channelId}>",
                            config.mentionId?.let { " (<@$it>)" } ?: "",
                        ),
                        Locale.JAPAN,
                    ),
                    listOfNotNull(
                        Button.secondary(
                            getComponentId("set", ComponentReplayType.EDIT),
                            messages.getMessage(
                                "button.bump.set",
                                null,
                                Locale.JAPAN,
                            ),
                        ),
                        Button.secondary(
                            getComponentId("unset", ComponentReplayType.EDIT),
                            messages.getMessage(
                                "button.bump.unset",
                                null,
                                Locale.JAPAN,
                            ),
                        ),
                        Button.secondary(
                            getComponentId("set_mention", ComponentReplayType.EDIT),
                            messages.getMessage(
                                "button.bump.set_mention",
                                null,
                                Locale.JAPAN,
                            ),
                        ),
                        config.mentionId?.let {
                            Button.secondary(
                                getComponentId("unset_mention", ComponentReplayType.EDIT),
                                messages.getMessage(
                                    "button.bump.unset_mention",
                                    null,
                                    Locale.JAPAN,
                                ),
                            )
                        },
                    ),
                )
            }
        }

        override fun onButton(event: BotButtonData) {
            if (event.actionData.key == "set") {
                configRepository.save(
                    BumpConfigData(
                        guildId = event.guild.idLong,
                        channelId = event.event.channel.idLong,
                        mentionId = null,
                    ),
                )
                event.edit(
                    messages.getMessage(
                        "command.message.bump.seted",
                        arrayOf(event.event.channel.asMention),
                        Locale.JAPAN,
                    ),
                )
                return
            }

            val config = configRepository.findByGuildId(event.guild.idLong)

            if (config == null) {
                event.edit(
                    messages.getMessage(
                        "command.message.bump.none",
                        null,
                        Locale.JAPAN,
                    ),
                )
                return
            }

            when (event.actionData.key) {
                "unset" -> {
                    event.edit(
                        messages.getMessage(
                            "command.message.bump.unseted",
                            arrayOf(
                                "<#${config.channelId}>",
                                config.mentionId?.let { " ($it)" } ?: "",
                            ),
                            Locale.JAPAN,
                        ),
                    )
                    configRepository.delete(config)
                }
                "set_mention" ->
                    event.edit(
                        messages.getMessage("command.message.bump.set_mention", null, Locale.JAPAN),
                        listOf(
                            EntitySelectMenu.create(getComponentId("select_mention"), EntitySelectMenu.SelectTarget.ROLE, EntitySelectMenu.SelectTarget.USER).build(),
                        ),
                    )
                "unset_mention" -> {
                    event.edit(
                        messages.getMessage(
                            "command.message.bump.mention.unseted",
                            null,
                            Locale.JAPAN,
                        ),
                    )
                    configRepository.save(config.copy(mentionId = null))
                }
            }
        }

        override fun onEntitySelect(event: BotEntitySelectData) {
            val config = configRepository.findByGuildId(event.guild.idLong) ?: return

            if (event.actionData.key == "select_mention") {
                val mentionId = event.values.first()
                configRepository.save(config.copy(mentionId = mentionId.idLong))
                // TODO: Editに変更
                event.reply(
                    messages.getMessage(
                        "command.message.bump.mention.seted",
                        arrayOf(mentionId.asMention),
                        Locale.JAPAN,
                    ),
                )
            }
        }
    }
