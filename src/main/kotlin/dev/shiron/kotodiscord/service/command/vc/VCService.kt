package dev.shiron.kotodiscord.service.command.vc

import dev.shiron.kotodiscord.domain.VCNotificationData
import dev.shiron.kotodiscord.repository.VCNotificationDataRepository
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*

@Service
class VCService
    @Autowired
    constructor(
        private val vcRepository: VCNotificationDataRepository,
        private val messages: MessageSource,
    ) : ListenerAdapter() {
        fun setVCNotification(data: VCNotificationData): VCNotificationData {
            return vcRepository.save(data)
        }

        fun removeVCNotification(data: VCNotificationData) {
            vcRepository.delete(data)
        }

        fun listVCNotification(guildId: Long): List<VCNotificationData> {
            return vcRepository.findAllByGuildId(guildId) ?: emptyList()
        }

        override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
            val guild = event.guild
            val allData = vcRepository.findAllByGuildId(guild.idLong) ?: return

            if (event.channelJoined != null) {
                val joinData = allData.filter { it.vcChannelId == event.channelJoined?.idLong || it.vcCategoryId == event.channelJoined?.parentCategoryIdLong || (it.vcCategoryId == null && it.vcChannelId == null) }

                for (data in joinData) {
                    guild.getTextChannelById(data.textChannelId)?.sendMessage(messages.getMessage("service.message.vc_notification.join", arrayOf(event.member.asMention, data.vcName), Locale.JAPAN))?.queue()
                }
            }
            if (event.channelLeft != null) {
                val leftData = allData.filter { it.vcChannelId == event.channelLeft?.idLong || it.vcCategoryId == event.channelLeft?.parentCategoryIdLong || (it.vcCategoryId == null && it.vcChannelId == null) }

                for (data in leftData) {
                    guild.getTextChannelById(data.textChannelId)?.sendMessage(messages.getMessage("service.message.vc_notification.left", arrayOf(event.member.asMention, data.vcName), Locale.JAPAN))?.queue()
                }
            }
        }
    }
