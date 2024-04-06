package dev.shiron.kotodiscord.service.command.vc.notify

import dev.shiron.kotodiscord.i18n.I18n
import dev.shiron.kotodiscord.util.data.action.BotSlashCommandData
import dev.shiron.kotodiscord.util.meta.SubCommandEnum
import dev.shiron.kotodiscord.util.service.SubCommandServiceClass
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ListCommand
    @Autowired
    constructor(
        private val vcService: VCService,
        private val i18n: I18n,
    ) : SubCommandServiceClass(
            SubCommandEnum.VC_NOTIFICATION_LIST,
            i18n,
        ) {
        override fun onSlashCommand(cmd: BotSlashCommandData) {
            val vcData = vcService.listVCNotification(cmd.guild.idLong)
            cmd.event.hook.sendMessage(
                i18n.format("command.message.vc_notification.list") +
                    "\n" +
                    vcData.withIndex().joinToString("\n") {
                        "- ${it.index + 1}:  ${it.value.vcName} -> <#${it.value.textChannelId}>"
                    },
            ).queue()
        }
    }
