package dev.shiron.kotodiscord.service.command.vc

import dev.shiron.kotodiscord.domain.VCNotificationData
import dev.shiron.kotodiscord.util.BotSlashCommandData
import dev.shiron.kotodiscord.util.SubCommandServiceClass
import dev.shiron.kotodiscord.util.meta.SubCommandEnum
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*

@Service
class ListCommand
    @Autowired
    constructor(
        private val vcService: VCService,
        private val messages: MessageSource,
    ) : SubCommandServiceClass(
            SubCommandEnum.VC_NOTIFICATION_LIST,
            messages,
        ) {
        override fun onSlashCommand(cmd: BotSlashCommandData) {
            val vcData = vcService.listVCNotification(cmd.guild.idLong)
            cmd.event.hook.sendMessage(
                "> VC通知設定リスト\n" +
                    vcData.withIndex().joinToString("\n") {
                        "- ${it.index + 1}:  ${getVCName(it.value)} -> <#${it.value.textChannelId}>"
                    },
            ).addActionRow(
                Button.primary(
                    "rm",
                    messages.getMessage(
                        "command.button.vc_notification.list.rm",
                        arrayOf(),
                        Locale.JAPAN,
                    ),
                )
                    .withEmoji(
                        Emoji.fromUnicode("🗑️"),
                    ),
            ).queue()
        }

        override fun onButtonInteraction(event: ButtonInteractionEvent) {
            // TODO: onSlachのように、Controllerから呼び出すようにする
            val guild = event.guild ?: return
            when (event.componentId) {
                "rm" -> {
                    event.reply("削除する設定の番号を選択してください").addActionRow(
                        StringSelectMenu.create("rm").apply {
                            vcService.listVCNotification(guild.idLong).forEachIndexed { index, vcNotificationData ->
                                val vcId = vcNotificationData.vcCategoryId ?: vcNotificationData.vcChannelId
                                val vcName = vcId.let { guild.channels.find { it.idLong == vcId }?.name }?.let { "#$it" } ?: "サーバー全体"
                                val textName = guild.channels.find { it.idLong == vcNotificationData.textChannelId }?.name?.let { "#$it" } ?: "不明"
                                addOption(
                                    "${index + 1} $vcName -> $textName",
                                    "${index + 1}",
                                )
                            }
                        }.build(),
                    ).queue()
                }
            }
        }

        override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
            // TODO: セレクト時からdataが変更されていないことを確認する
            when (event.componentId) {
                "rm" -> {
                    val guild = event.guild ?: return
                    val index = event.values.first().toIntOrNull()?.minus(1) ?: return
                    val data = vcService.listVCNotification(guild.idLong)[index]
                    vcService.removeVCNotification(data)
                    event.reply("$index : ${getVCName(data)}  -> <#${data.textChannelId}>\n" + "の設定を削除しました").queue()
                }
            }
        }

        private fun getVCName(data: VCNotificationData): String {
            val vcId = data.vcCategoryId ?: data.vcChannelId
            return if (vcId == null) "`サーバー全体`" else "<#$vcId>"
        }
    }
