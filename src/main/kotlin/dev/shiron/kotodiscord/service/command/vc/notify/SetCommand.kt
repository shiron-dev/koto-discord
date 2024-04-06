package dev.shiron.kotodiscord.service.command.vc.notify

import dev.shiron.kotodiscord.domain.VCNotificationData
import dev.shiron.kotodiscord.i18n.I18n
import dev.shiron.kotodiscord.util.data.action.BotSlashCommandData
import dev.shiron.kotodiscord.util.meta.SubCommandEnum
import dev.shiron.kotodiscord.util.service.SubCommandServiceClass
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SetCommand
    @Autowired
    constructor(
        private val vcService: VCService,
        private val i18n: I18n,
    ) : SubCommandServiceClass(
            SubCommandEnum.VC_NOTIFICATION_SET,
            i18n,
        ) {
        override val commandOptions: List<OptionData>
            get() =
                listOf(
                    OptionData(
                        OptionType.CHANNEL,
                        "text",
                        i18n.format("command.option.vc_notification.set.text"),
                        true,
                    ),
                    OptionData(
                        OptionType.CHANNEL,
                        "vc",
                        i18n.format("command.option.vc_notification.set.vc"),
                    ),
                )

        override fun onSlashCommand(cmd: BotSlashCommandData) {
            val dataList = vcService.listVCNotification(cmd.guild.idLong)
            if (dataList.size >= 10) {
                cmd.reply(i18n.format("command.message.vc_notification.set.limit"))
                return
            }

            val vc = cmd.event.getOption("vc")?.asChannel
            val text = cmd.event.getOption("text")?.asChannel
            if (text == null) {
                cmd.reply(i18n.format("command.error.internal"))
                return
            }

            if (vc?.type != ChannelType.VOICE && vc?.type != ChannelType.STAGE && vc?.type != ChannelType.CATEGORY && vc != null) {
                cmd.reply(i18n.format("command.message.vc_notification.set.vc"))
                return
            }
            if (text.type != ChannelType.TEXT && text.type != ChannelType.NEWS && text.type != ChannelType.FORUM) {
                cmd.reply(i18n.format("command.message.vc_notification.set.text"))
                return
            }

            val register =
                VCNotificationData(
                    guildId = cmd.guild.idLong,
                    vcCategoryId = if (vc?.type == ChannelType.CATEGORY) vc.idLong else null,
                    vcChannelId = if (vc?.type == ChannelType.VOICE) vc.idLong else null,
                    textChannelId = text.idLong,
                )
            for (data in dataList) {
                if (data.like(register)) {
                    cmd.reply(i18n.format("command.message.vc_notification.set.already"))
                    return
                }
            }

            vcService.setVCNotification(register)
            cmd.reply(
                i18n.format(
                    "command.message.vc_notification.set.success",
                    vc?.asMention ?: "サーバー全体",
                    text.asMention,
                ),
            )
        }
    }
