package dev.shiron.kotodiscord.service.command.vc

import dev.shiron.kotodiscord.domain.VCNotificationData
import dev.shiron.kotodiscord.repository.VCNotificationDataRepository
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
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
            val textChannelList = mutableListOf<Long>()

            for (data in allData) {
                val isJoin =
                    event.channelJoined != null && (
                        data.vcChannelId == event.channelJoined?.idLong ||
                            data.vcCategoryId == event.channelJoined?.parentCategoryIdLong
                    )
                val isLeft =
                    event.channelLeft != null && (
                        data.vcChannelId == event.channelLeft?.idLong ||
                            data.vcCategoryId == event.channelLeft?.parentCategoryIdLong
                    )
                val isAll = data.vcChannelId == null && data.vcCategoryId == null

                if (isJoin || isLeft || isAll) {
                    textChannelList.add(data.textChannelId)
                }
            }

            val eb = EmbedBuilder()
            eb.setAuthor(event.member.effectiveName, null, event.member.effectiveAvatarUrl)

            val date = Date()
            val timeZoneJP = TimeZone.getTimeZone("Asia/Tokyo")
            val fmt = SimpleDateFormat()
            fmt.timeZone = timeZoneJP
            eb.setFooter(fmt.format(date))

            val isJoin = event.channelJoined != null
            val isLeft = event.channelLeft != null
            if (isJoin && isLeft) {
                eb.setTitle("Change")
                eb.setDescription(messages.getMessage("service.message.vc_notification.change", arrayOf(event.member.asMention, event.channelLeft?.asMention, event.channelJoined?.asMention), Locale.JAPAN))
            } else if (isJoin) {
                eb.setTitle("Join")
                eb.setDescription(messages.getMessage("service.message.vc_notification.join", arrayOf(event.member.asMention, event.channelJoined?.asMention), Locale.JAPAN))
            } else if (isLeft) {
                eb.setTitle("Left")
                eb.setDescription(messages.getMessage("service.message.vc_notification.left", arrayOf(event.member.asMention, event.channelLeft?.asMention), Locale.JAPAN))
            }

            for (textChannel in textChannelList) {
                guild.getTextChannelById(textChannel)?.sendMessage("")?.setEmbeds(eb.build())?.queue()
            }

            // guild.getTextChannelById(data.textChannelId)?.sendMessage(messages.getMessage("service.message.vc_notification.join", arrayOf(event.member.asMention, data.vcName), Locale.JAPAN))?.queue()
        }
    }
