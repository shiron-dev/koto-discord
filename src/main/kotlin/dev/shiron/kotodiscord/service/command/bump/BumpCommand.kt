package dev.shiron.kotodiscord.service.command.bump

import dev.shiron.kotodiscord.bot.KotoMain
import dev.shiron.kotodiscord.domain.BumpConfigData
import dev.shiron.kotodiscord.domain.BumpJobQueueData
import dev.shiron.kotodiscord.repository.BumpConfigDataRepository
import dev.shiron.kotodiscord.repository.BumpJobQueueDataRepository
import dev.shiron.kotodiscord.util.SingleCommandServiceClass
import dev.shiron.kotodiscord.util.data.BotButtonData
import dev.shiron.kotodiscord.util.data.BotEntitySelectData
import dev.shiron.kotodiscord.util.data.BotSlashCommandData
import dev.shiron.kotodiscord.util.data.ComponentReplayType
import dev.shiron.kotodiscord.util.meta.SingleCommandEnum
import dev.shiron.kotodiscord.vars.BumpVars
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class BumpCommand
    @Autowired
    constructor(
        private val configRepository: BumpConfigDataRepository,
        private val jobQueueRepository: BumpJobQueueDataRepository,
        private val messages: MessageSource,
    ) : SingleCommandServiceClass(
            SingleCommandEnum.BUMP,
            messages,
        ) {
        val btnSet =
            Button.secondary(
                getComponentId("set", ComponentReplayType.EDIT),
                messages.getMessage(
                    "button.bump.set",
                    null,
                    Locale.JAPAN,
                ),
            )
        val btnUnset =
            Button.secondary(
                getComponentId("unset", ComponentReplayType.EDIT),
                messages.getMessage(
                    "button.bump.unset",
                    null,
                    Locale.JAPAN,
                ),
            )
        val btnSetMention =
            Button.secondary(
                getComponentId("set_mention", ComponentReplayType.EDIT),
                messages.getMessage(
                    "button.bump.set_mention",
                    null,
                    Locale.JAPAN,
                ),
            )
        val btnUnsetMention =
            Button.secondary(
                getComponentId("unset_mention", ComponentReplayType.EDIT),
                messages.getMessage(
                    "button.bump.unset_mention",
                    null,
                    Locale.JAPAN,
                ),
            )

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
                        btnSet,
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

                cmd.reply(
                    messages.getMessage(
                        "command.message.bump.msg",
                        arrayOf(
                            "<#${config.channelId}>",
                            config.mentionId?.let { " (<@$it>)" } ?: "",
                            if (LocalDateTime.now().isAfter(job.execAt)) {
                                messages.getMessage("command.message.bump.after", arrayOf(BumpVars.BUMP_COMMAND_MENTION), Locale.JAPAN)
                            } else {
                                messages.getMessage(
                                    "command.message.bump.before",
                                    arrayOf(
                                        job.execAt.until(LocalDateTime.now(), ChronoUnit.MINUTES) + 1,
                                    ),
                                    Locale.JAPAN,
                                )
                            },
                        ),
                        Locale.JAPAN,
                    ),
                    listOfNotNull(
                        btnSet,
                        btnUnset,
                        btnSetMention,
                        config.mentionId?.let {
                            btnUnsetMention
                        },
                    ),
                )
            }
        }

        override fun onButton(event: BotButtonData) {
            if (event.actionData.key == "set") {
                val config =
                    configRepository.save(
                        BumpConfigData(
                            guildId = event.guild.idLong,
                            channelId = event.event.channel.idLong,
                            mentionId = null,
                        ),
                    )
                jobQueueRepository.save(
                    BumpJobQueueData(
                        bumpConfig = config,
                        createAt = LocalDateTime.now(),
                        execAt = LocalDateTime.now().plusMinutes(BumpVars.BUMP_NOTIFY_MIN.toLong()),
                    ),
                )
                event.edit(
                    messages.getMessage(
                        "command.message.bump.seted",
                        arrayOf(event.event.channel.asMention),
                        Locale.JAPAN,
                    ),
                    listOf(
                        btnUnset,
                        btnSetMention,
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
                    listOf(
                        btnSet,
                        btnSetMention,
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
                                config.mentionId?.let { " (<@$it>)" } ?: "",
                            ),
                            Locale.JAPAN,
                        ),
                    )
                    jobQueueRepository.deleteByBumpConfig(config)
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

        @Scheduled(fixedDelay = BumpVars.BUMP_SCHEDULE_MS)
        fun bumpNotifyCommand() {
            val now = LocalDateTime.now()
            val jobs = jobQueueRepository.findAllByExecAtBefore(now) ?: return

            for (job in jobs) {
                val config = job.bumpConfig
                val mention = config.mentionId?.let { "<@$it> " } ?: ""

                try {
                    KotoMain.jda.getGuildById(config.guildId)?.getTextChannelById(config.channelId)?.sendMessage(
                        mention +
                            messages.getMessage(
                                "service.message.bump",
                                arrayOf(BumpVars.BUMP_COMMAND_MENTION),
                                Locale.JAPAN,
                            ),
                    )?.queue()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                jobQueueRepository.delete(job)
            }
        }
    }
