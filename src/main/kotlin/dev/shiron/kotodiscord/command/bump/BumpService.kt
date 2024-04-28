package dev.shiron.kotodiscord.command.bump

import dev.shiron.kotodiscord.bot.KotoMain
import dev.shiron.kotodiscord.domain.BumpJobQueueData
import dev.shiron.kotodiscord.i18n.I18n
import dev.shiron.kotodiscord.repository.BumpConfigDataRepository
import dev.shiron.kotodiscord.repository.BumpJobQueueDataRepository
import dev.shiron.kotodiscord.util.getMentionable
import dev.shiron.kotodiscord.vars.BumpVars
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class BumpService
    @Autowired
    constructor(
        private val configRepository: BumpConfigDataRepository,
        private val jobQueueRepository: BumpJobQueueDataRepository,
        private val i18n: I18n,
    ) : ListenerAdapter() {
        override fun onMessageReceived(event: MessageReceivedEvent) {
            if (event.author.isBot && event.author.idLong == BumpVars.DISBOARD_USER_ID) {
                val msg = event.message.embeds.first().description.toString()
                if (msg.trim().startsWith(BumpVars.BUMP_OK_MESSAGE)) {
                    val config = configRepository.findByGuildId(event.guild.idLong) ?: return
                    val job = jobQueueRepository.findByBumpConfig(config)

                    if (job == null) {
                        jobQueueRepository.save(
                            BumpJobQueueData(
                                bumpConfig = config,
                                createAt = LocalDateTime.now(),
                                execAt = LocalDateTime.now().plusMinutes(BumpVars.BUMP_NOTIFY_MIN.toLong()),
                            ),
                        )
                    } else {
                        jobQueueRepository.deleteByBumpConfig(config)
                        jobQueueRepository.save(job.copy(execAt = LocalDateTime.now().plusMinutes(BumpVars.BUMP_NOTIFY_MIN.toLong())))
                    }
                }
            }
        }

        @Scheduled(fixedDelay = BumpVars.BUMP_SCHEDULE_MS)
        fun bumpNotifyCommand() {
            val now = LocalDateTime.now()
            val jobs = jobQueueRepository.findAllByExecAtBefore(now) ?: return

            for (job in jobs) {
                val config = job.bumpConfig
                val guild = KotoMain.jda.getGuildById(config.guildId)
                val mention = guild?.let { getMentionable(it, config.mentionId) }?.asMention

                try {
                    guild?.getTextChannelById(config.channelId)?.sendMessage(
                        (mention ?: "") +
                            i18n.format(
                                "service.message.bump",
                                BumpVars.BUMP_COMMAND_MENTION,
                            ),
                    )?.queue()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                jobQueueRepository.delete(job)
            }
        }
    }
