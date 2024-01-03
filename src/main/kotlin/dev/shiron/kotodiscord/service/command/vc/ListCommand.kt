package dev.shiron.kotodiscord.service.command.vc

import dev.shiron.kotodiscord.util.BotSlashCommandData
import dev.shiron.kotodiscord.util.SubCommandServiceClass
import dev.shiron.kotodiscord.util.meta.SubCommandEnum
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service

@Service
class ListCommand @Autowired constructor(private val vcService: VCService, messages: MessageSource) : SubCommandServiceClass(SubCommandEnum.VC_NOTIFICATION_LIST, messages) {
    override fun onSlashCommand(cmd: BotSlashCommandData) {
        val vcData = vcService.listVCNotification(cmd.guild.idLong)
        cmd.reply(
            "> VC通知設定リスト\n" +
                vcData.joinToString("\n") {
                    val vcId = it.vcCategoryId ?: it.vcChannelId
                    "- ${if (vcId == null)"`サーバー全体`" else "<#$vcId>" } -> <#${it.textChannelId}>"
                }
        )
    }
}
