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

            data class TextData(
                val textChannelId: Long,
                val isJoin: Boolean,
                val isLeft: Boolean,
            )

            val textDataList = mutableListOf<TextData>()

            for (data in allData) {
                val isJoin =
                    event.channelJoined != null && (
                        data.vcChannelId == event.channelJoined?.idLong ||
                            data.vcCategoryId == event.channelJoined?.parentCategoryIdLong ||
                            (data.vcCategoryId == null && data.vcChannelId == null)
                    )
                val isLeft =
                    event.channelLeft != null && (
                        data.vcChannelId == event.channelLeft?.idLong ||
                            data.vcCategoryId == event.channelLeft?.parentCategoryIdLong ||
                            (data.vcCategoryId == null && data.vcChannelId == null)
                    )

                if (isJoin || isLeft) {
                    textDataList.add(
                        TextData(
                            data.textChannelId,
                            isJoin,
                            isLeft,
                        ),
                    )
                }
            }

            val eb = EmbedBuilder()
            eb.setAuthor(event.member.effectiveName, null, event.member.effectiveAvatarUrl)

            val date = Date()
            val timeZoneJP = TimeZone.getTimeZone("Asia/Tokyo")
            val fmt = SimpleDateFormat()
            fmt.timeZone = timeZoneJP
            eb.setFooter(fmt.format(date))

            for (textData in textDataList) {
                if (textData.isJoin && textData.isLeft) {
                    eb.setTitle("Change")
                    eb.setDescription(messages.getMessage("service.message.vc_notification.change", arrayOf(event.member.asMention, event.channelLeft?.asMention, event.channelJoined?.asMention), Locale.JAPAN))
                } else if (textData.isJoin) {
                    eb.setTitle("Join")
                    eb.setDescription(messages.getMessage("service.message.vc_notification.join", arrayOf(event.member.asMention, event.channelJoined?.asMention), Locale.JAPAN))
                } else if (textData.isLeft) {
                    eb.setTitle("Left")
                    eb.setDescription(messages.getMessage("service.message.vc_notification.left", arrayOf(event.member.asMention, event.channelLeft?.asMention), Locale.JAPAN))
                }
                guild.getTextChannelById(textData.textChannelId)?.sendMessage("")?.setEmbeds(eb.build())?.queue()
            }
        }
    }
