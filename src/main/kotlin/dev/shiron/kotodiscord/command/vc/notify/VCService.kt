package dev.shiron.kotodiscord.command.vc.notify

import dev.shiron.kotodiscord.domain.VCNotificationData
import dev.shiron.kotodiscord.i18n.I18n
import dev.shiron.kotodiscord.repository.VCNotificationDataRepository
import dev.shiron.kotodiscord.util.getMentionable
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.awt.Color
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.min

@Service
class VCService
    @Autowired
    constructor(
        private val vcRepository: VCNotificationDataRepository,
        private val i18n: I18n,
    ) : ListenerAdapter() {
        fun setVCNotification(data: VCNotificationData): VCNotificationData {
            VCSmartNotifyManager.remove(data)
            return vcRepository.save(data)
        }

        fun removeVCNotification(data: VCNotificationData) {
            VCSmartNotifyManager.remove(data)
            vcRepository.delete(data)
        }

        fun listVCNotification(guildId: Long): List<VCNotificationData> {
            return vcRepository.findAllByGuildId(guildId) ?: emptyList()
        }

        override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
            val guild = event.guild
            val allData = vcRepository.findAllByGuildId(guild.idLong) ?: return

            data class NonSmartTextData(
                val textChannelId: Long,
                val isJoin: Boolean,
                val isLeft: Boolean,
                val config: VCNotificationData,
            )

            data class SmartTextData(
                val textChannelId: Long,
                val firstJoin: Boolean,
                val lastLeft: Boolean,
                val members: List<Member>,
                val config: VCNotificationData,
            )

            val nonSmartTextDataList = mutableListOf<NonSmartTextData>()
            val smartTextDataList = mutableListOf<SmartTextData>()

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
                    if (!data.isSmart) {
                        if (isJoin || isLeft) {
                            nonSmartTextDataList.add(
                                NonSmartTextData(
                                    data.textChannelId,
                                    isJoin,
                                    isLeft,
                                    data,
                                ),
                            )
                        }
                    } else {
                        val channels = listOfNotNull(event.channelLeft, event.channelJoined)
                        for (channel in channels) {
                            val members = channel.members
                            smartTextDataList.add(
                                SmartTextData(
                                    data.textChannelId,
                                    members.size == 1 && event.channelJoined != null,
                                    members.isEmpty() && event.channelLeft != null,
                                    members,
                                    data,
                                ),
                            )
                        }
                    }
                }
            }

            val date = LocalDateTime.now()
            val fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")

            for (textData in nonSmartTextDataList) {
                val eb = EmbedBuilder()
                eb.setAuthor(event.member.effectiveName, null, event.member.effectiveAvatarUrl)
                eb.setFooter(fmt.format(date))

                if (textData.isJoin && textData.isLeft) {
                    eb.setTitle("Change")
                    eb.setDescription(
                        i18n.format(
                            "service.message.vc_notification.change",
                            event.guild.name,
                            event.member.effectiveName,
                            event.channelJoined?.name ?: "",
                            event.member.asMention,
                            event.channelLeft?.asMention ?: "",
                            event.channelJoined?.asMention ?: "",
                        ),
                    )
                } else if (textData.isJoin) {
                    eb.setTitle("Join")
                    eb.setDescription(
                        i18n.format(
                            "service.message.vc_notification.join",
                            event.guild.name,
                            event.member.effectiveName,
                            event.channelJoined?.name ?: "",
                            event.member.asMention,
                            event.channelJoined?.asMention ?: "",
                        ),
                    )
                } else if (textData.isLeft) {
                    eb.setTitle("Left")
                    eb.setDescription(
                        i18n.format(
                            "service.message.vc_notification.left",
                            event.guild.name,
                            event.member.effectiveName,
                            event.channelLeft?.name ?: "",
                            event.member.asMention,
                            event.channelLeft?.asMention ?: "",
                        ),
                    )
                }

                val mention = getMentionable(guild, textData.config.mentionId)?.asMention
                guild.getTextChannelById(textData.textChannelId)?.sendMessage(mention ?: "")?.setEmbeds(eb.build())?.queue()
            }

            for (textData in smartTextDataList) {
                val eb = EmbedBuilder()
                eb.setAuthor(event.member.effectiveName, null, event.member.effectiveAvatarUrl)
                eb.setFooter(fmt.format(date))

                val names =
                    run {
                        val names = textData.members.joinToString(", ") { it.effectiveName }
                        val last = if (names.length > 100) "..." else ""
                        return@run names.substring(0, min(names.length, 100)) + last
                    }

                if (!textData.lastLeft) {
                    eb.setTitle(i18n.format("service.message.smart.status.active"))
                    eb.setColor(Color.GREEN)
                } else {
                    eb.setTitle(i18n.format("service.message.smart.status.inactive"))
                    eb.setColor(Color.RED)
                }

                val started = VCSmartNotifyManager[textData.config]?.startDate ?: date

                eb.setDescription(
                    i18n.format(
                        "service.message.smart.message",
                        (event.channelJoined ?: event.channelLeft)?.asMention ?: "#不明",
                        event.guild.name,
                        textData.members.size.toString(),
                        names,
                        fmt.format(started),
                        started.until(date, ChronoUnit.MINUTES).toString(),
                    ),
                )

                val message = VCSmartNotifyManager[textData.config]?.message
                if (message == null) {
                    val mention = getMentionable(guild, textData.config.mentionId)?.asMention
                    guild.getTextChannelById(textData.textChannelId)?.sendMessage(mention ?: "")?.setEmbeds(eb.build())?.queue {
                        VCSmartNotifyManager.new(
                            VCSmartNotifyData(
                                textData.config,
                                it,
                                date,
                            ),
                        )
                    }
                } else {
                    message.editMessageEmbeds(eb.build()).queue()
                }
                if (textData.lastLeft) {
                    VCSmartNotifyManager.remove(textData.config)
                }
            }
        }
    }
