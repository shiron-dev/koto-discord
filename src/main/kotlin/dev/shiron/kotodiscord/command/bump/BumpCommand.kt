package dev.shiron.kotodiscord.command.bump

import dev.shiron.kotodiscord.domain.BumpConfigData
import dev.shiron.kotodiscord.domain.BumpJobQueueData
import dev.shiron.kotodiscord.i18n.I18n
import dev.shiron.kotodiscord.repository.BumpConfigDataRepository
import dev.shiron.kotodiscord.repository.BumpJobQueueDataRepository
import dev.shiron.kotodiscord.util.data.action.BotButtonData
import dev.shiron.kotodiscord.util.data.action.BotEntitySelectData
import dev.shiron.kotodiscord.util.data.action.BotSlashCommandData
import dev.shiron.kotodiscord.util.data.action.ComponentSendType
import dev.shiron.kotodiscord.util.meta.SingleCommandEnum
import dev.shiron.kotodiscord.util.service.SingleCommandServiceClass
import dev.shiron.kotodiscord.vars.BumpVars
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
class BumpCommand
    @Autowired
    constructor(
        private val configRepository: BumpConfigDataRepository,
        private val jobQueueRepository: BumpJobQueueDataRepository,
        private val i18n: I18n,
    ) : SingleCommandServiceClass(
            SingleCommandEnum.BUMP,
            i18n,
        ) {
        fun genBtnSet(shared: Boolean): Button {
            return Button.secondary(
                genComponentId("set", shared, ComponentSendType.EDIT),
                i18n.format("button.bump.set"),
            )
        }

        fun genBtnUnset(shared: Boolean): Button {
            return Button.secondary(
                genComponentId("unset", shared, ComponentSendType.EDIT),
                i18n.format("button.bump.unset"),
            )
        }

        fun genBtnSetMention(shared: Boolean): Button {
            return Button.secondary(
                genComponentId("set_mention", shared, ComponentSendType.EDIT),
                i18n.format("button.bump.set_mention"),
            )
        }

        fun genBtnUnsetMention(shared: Boolean): Button {
            return Button.secondary(
                genComponentId("unset_mention", shared, ComponentSendType.EDIT),
                i18n.format("button.bump.unset_mention"),
            )
        }

        override fun onSlashCommand(cmd: BotSlashCommandData) {
            val config = configRepository.findByGuildId(cmd.guild.idLong)

            if (config == null) {
                cmd.send(
                    i18n.format("command.message.bump.none"),
                    listOf(
                        genBtnSet(cmd.shared),
                    ),
                )
            } else {
                val job =
                    jobQueueRepository.findByBumpConfig(config) ?: jobQueueRepository.save(
                        BumpJobQueueData(
                            bumpConfig = config,
                            createAt = LocalDateTime.now(),
                            execAt = LocalDateTime.now().plusMinutes(BumpVars.BUMP_NOTIFY_MIN.toLong()),
                        ),
                    )

                cmd.send(
                    i18n.format(
                        "command.message.bump.msg",
                        cmd.guild.getTextChannelById(config.channelId)?.asMention ?: "",
                        config.mentionId?.let { " (<@$it>)" } ?: "",
                        if (LocalDateTime.now().isAfter(job.execAt)) {
                            i18n.format("command.message.bump.after", BumpVars.BUMP_COMMAND_MENTION)
                        } else {
                            i18n.format(
                                "command.message.bump.before",
                                (LocalDateTime.now().until(job.execAt, ChronoUnit.MINUTES) + 1).toString(),
                            )
                        },
                    ),
                    listOfNotNull(
                        if (cmd.event.channelIdLong != config.channelId) genBtnSet(cmd.shared) else null,
                        genBtnUnset(cmd.shared),
                        genBtnSetMention(cmd.shared),
                        config.mentionId?.let {
                            genBtnUnsetMention(cmd.shared)
                        },
                    ),
                )
            }
        }

        override fun onButton(event: BotButtonData) {
            val config = configRepository.findByGuildId(event.guild.idLong)

            if (event.actionData.key == "set") {
                val newConfig =
                    config ?: configRepository.save(
                        BumpConfigData(
                            guildId = event.guild.idLong,
                            channelId = event.event.channel.idLong,
                            mentionId = null,
                        ),
                    )
                newConfig.channelId = event.event.channel.idLong

                jobQueueRepository.deleteByBumpConfig(newConfig)
                jobQueueRepository.save(
                    BumpJobQueueData(
                        bumpConfig = newConfig,
                        createAt = LocalDateTime.now(),
                        execAt = LocalDateTime.now().plusMinutes(BumpVars.BUMP_NOTIFY_MIN.toLong()),
                    ),
                )
                event.send(
                    i18n.format(
                        "command.message.bump.seted",
                        event.event.channel.asMention,
                    ),
                    listOf(
                        genBtnUnset(event.actionData.isShow),
                        genBtnSetMention(event.actionData.isShow),
                    ),
                )
                return
            }

            if (config == null) {
                event.send(
                    i18n.format("command.message.bump.none"),
                    listOf(
                        genBtnSet(event.actionData.isShow),
                        genBtnSetMention(event.actionData.isShow),
                    ),
                )
                return
            }

            when (event.actionData.key) {
                "unset" -> {
                    event.send(
                        i18n.format(
                            "command.message.bump.unseted",
                            event.guild.getTextChannelById(config.channelId)?.asMention ?: "",
                            config.mentionId?.let { " (<@$it>)" } ?: "",
                        ),
                    )
                    jobQueueRepository.deleteByBumpConfig(config)
                    configRepository.delete(config)
                }
                "set_mention" ->
                    event.send(
                        i18n.format("command.message.bump.set_mention"),
                        listOf(
                            EntitySelectMenu.create(genComponentId("select_mention", event.actionData.isShow, ComponentSendType.EDIT), EntitySelectMenu.SelectTarget.ROLE, EntitySelectMenu.SelectTarget.USER).build(),
                        ),
                    )
                "unset_mention" -> {
                    event.send(
                        i18n.format("command.message.bump.mention.unseted"),
                    )
                    configRepository.save(config.copy(mentionId = null))
                }
            }
        }

        override fun onEntitySelect(event: BotEntitySelectData) {
            val config = configRepository.findByGuildId(event.guild.idLong) ?: return

            if (event.actionData.key == "select_mention") {
                val mentionId = event.values.first()

                if (event.guild.getTextChannelById(config.channelId) == null) {
                    event.send(
                        i18n.format(
                            "command.message.bump.error.text",
                        ),
                    )
                    return
                }

                configRepository.save(config.copy(mentionId = mentionId.idLong))
                event.send(
                    i18n.format(
                        "command.message.bump.mention.seted",
                        mentionId.asMention,
                    ),
                )
            }
        }
    }
