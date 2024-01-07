package dev.shiron.kotodiscord.service.command.vc

import dev.shiron.kotodiscord.util.BotSlashCommandData
import dev.shiron.kotodiscord.util.SubCommandServiceClass
import dev.shiron.kotodiscord.util.meta.SubCommandEnum
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
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
            val options =
                StringSelectMenu.create("rm").apply {
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

        override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
            when (event.componentId) {
                "rm" -> {
                    val guild = event.guild ?: return
                    val index = event.values.first().toIntOrNull()?.minus(1) ?: return
                    val data = vcService.listVCNotification(guild.idLong)[index]
                    vcService.removeVCNotification(data)
                    event.reply("${index+1} : ${data.vcName}  -> <#${data.textChannelId}>\n" + "の設定を削除しました")
                        .setEphemeral(false).queue()
                }
            }
        }
    }
