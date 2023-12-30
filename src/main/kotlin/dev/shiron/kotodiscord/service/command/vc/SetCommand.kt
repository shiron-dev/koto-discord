package dev.shiron.kotodiscord.service.command.vc

import dev.shiron.kotodiscord.util.BotSlashCommandData
import dev.shiron.kotodiscord.util.SubCommandServiceClass
import dev.shiron.kotodiscord.util.meta.SubCommandEnum
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service

@Service
class SetCommand @Autowired constructor(
    messages: MessageSource
) : SubCommandServiceClass(
    SubCommandEnum.VC_NOTIFICATION_SET,
    messages
) {
    override fun onSlashCommand(cmd: BotSlashCommandData) {
        cmd.reply("set command")
    }
}
