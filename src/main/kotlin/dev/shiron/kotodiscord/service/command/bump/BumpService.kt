package dev.shiron.kotodiscord.service.command.bump

import dev.shiron.kotodiscord.domain.BumpJobQueueData
import dev.shiron.kotodiscord.repository.BumpConfigDataRepository
import dev.shiron.kotodiscord.repository.BumpJobQueueDataRepository
import dev.shiron.kotodiscord.vars.BumpVars
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class BumpService
    @Autowired
    constructor(
        private val configRepository: BumpConfigDataRepository,
        private val jobQueueRepository: BumpJobQueueDataRepository,
    ) : ListenerAdapter() {
        override fun onMessageReceived(event: MessageReceivedEvent) {
            if (event.author.isBot && event.author.idLong == BumpVars.DISBOARD_USER_ID) {
                val msg = event.message.embeds.first().description.toString()
                if (msg in BumpVars.BUMP_OK_MESSAGE) {
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
    }
