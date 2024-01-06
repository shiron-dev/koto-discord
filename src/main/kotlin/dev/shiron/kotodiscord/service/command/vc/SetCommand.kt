package dev.shiron.kotodiscord.service.command.vc

import dev.shiron.kotodiscord.domain.VCNotificationData
import dev.shiron.kotodiscord.util.BotSlashCommandData
import dev.shiron.kotodiscord.util.SubCommandServiceClass
import dev.shiron.kotodiscord.util.meta.SubCommandEnum
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*

@Service
class SetCommand
    @Autowired
    constructor(
        private val vcService: VCService,
        private val messages: MessageSource,
    ) : SubCommandServiceClass(
            SubCommandEnum.VC_NOTIFICATION_SET,
            messages,
        ) {
        override val commandOptions: List<OptionData>
            get() =
                listOf(
                    OptionData(
                        OptionType.CHANNEL,
                        "text",
                        messages.getMessage(
                            "command.option.vc_notification.set.text",
                            arrayOf(),
                            Locale.JAPAN,
                        ),
                        true,
                    ),
                    OptionData(
                        OptionType.CHANNEL,
                        "vc",
                        messages.getMessage(
                            "command.option.vc_notification.set.vc",
                            arrayOf(),
                            Locale.JAPAN,
                        ),
                    ),
                )

        override fun onSlashCommand(cmd: BotSlashCommandData) {
            // TODO: 重複登録を禁止する
            vcService.dataCount(cmd.guild.idLong).let {
                if (it > 10) {
                    cmd.reply(
                        messages.getMessage(
                            "command.message.vc_notification.set.limit",
                            arrayOf(),
                            Locale.JAPAN,
                        ),
                    )
                    return
                }
            }

            val vc = cmd.event.getOption("vc")?.asChannel
            val text = cmd.event.getOption("text")?.asChannel
            if (text == null) {
                cmd.reply(
                    messages.getMessage(
                        "command.error.internal",
                        arrayOf(),
                        Locale.JAPAN,
                    ),
                )
                return
            }

            if (vc?.type != ChannelType.VOICE && vc?.type != ChannelType.STAGE && vc?.type != ChannelType.CATEGORY && vc != null) {
                cmd.reply(
                    messages.getMessage(
                        "command.message.vc_notification.set.vc",
                        arrayOf(),
                        Locale.JAPAN,
                    ),
                )
                return
            }
            if (text.type != ChannelType.TEXT && text.type != ChannelType.NEWS && text.type != ChannelType.FORUM) {
                cmd.reply(
                    messages.getMessage(
                        "command.message.vc_notification.set.text",
                        arrayOf(),
                        Locale.JAPAN,
                    ),
                )
                return
            }

            vcService.setVCNotification(
                VCNotificationData(
                    guildId = cmd.guild.idLong,
                    vcCategoryId = if (vc?.type == ChannelType.CATEGORY) vc.idLong else null,
                    vcChannelId = if (vc?.type == ChannelType.VOICE) vc.idLong else null,
                    textChannelId = text.idLong,
                ),
            )
            cmd.reply(
                messages.getMessage(
                    "command.message.vc_notification.set.success",
                    arrayOf(vc?.asMention ?: "サーバー全体", text.asMention),
                    Locale.JAPAN,
                ),
            )
        }
    }
