package dev.shiron.kotodiscord.service.command.vc.notify

import dev.shiron.kotodiscord.util.service.SubCommandServiceClass
import dev.shiron.kotodiscord.util.data.action.BotSlashCommandData
import dev.shiron.kotodiscord.util.data.action.BotStringSelectData
import dev.shiron.kotodiscord.util.meta.SubCommandEnum
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*

@Service
class RemoveCommand
    @Autowired
    constructor(private val vcService: VCService, private val messages: MessageSource) : SubCommandServiceClass(SubCommandEnum.VC_NOTIFICATION_REMOVE, messages) {
        override fun onSlashCommand(cmd: BotSlashCommandData) {
            val vcData = vcService.listVCNotification(cmd.guild.idLong)
            if (vcData.isEmpty()) {
                cmd.reply(
                    messages.getMessage(
                        "command.message.vc_notification.remove.empty",
                        arrayOf(),
                        Locale.JAPAN,
                    ),
                )
                return
            }
            val options =
                StringSelectMenu.create(getComponentId("rm")).apply {
                    vcData.forEachIndexed { index, vcNotificationData ->
                        val vcId = vcNotificationData.vcCategoryId ?: vcNotificationData.vcChannelId
                        val vcName = vcId.let { cmd.guild.channels.find { it.idLong == vcId }?.name }?.let { "#$it" } ?: "サーバー全体"
                        val textName = cmd.guild.channels.find { it.idLong == vcNotificationData.textChannelId }?.name?.let { "#$it" } ?: "不明"
                        addOption(
                            "${index + 1} $vcName -> $textName",
                            "${index + 1}",
                        )
                    }
                }.build()

            cmd.event.hook.sendMessage(
                messages.getMessage(
                    "command.message.vc_notification.remove",
                    arrayOf(),
                    Locale.JAPAN,
                ),
            ).addActionRow(options).queue()
        }

        override fun onStringSelect(event: BotStringSelectData) {
            when (event.actionData.key) {
                "rm" -> {
                    val index = event.values.first().toIntOrNull()?.minus(1) ?: return
                    val data = vcService.listVCNotification(event.guild.idLong)[index]
                    vcService.removeVCNotification(data)
                    event.reply("${index + 1} : ${data.vcName}  -> <#${data.textChannelId}>\n" + "の設定を削除しました")
                }
            }
        }
    }
