package dev.shiron.kotodiscord.service.command.vc.notify

import dev.shiron.kotodiscord.util.data.action.BotSlashCommandData
import dev.shiron.kotodiscord.util.meta.SubCommandEnum
import dev.shiron.kotodiscord.util.service.SubCommandServiceClass
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
                messages.getMessage("command.message.vc_notification.list", arrayOf(), Locale.JAPAN) +
                    "\n" +
                    vcData.withIndex().joinToString("\n") {
                        "- ${it.index + 1}:  ${it.value.vcName} -> <#${it.value.textChannelId}>"
                    },
            ).queue()
        }
    }
