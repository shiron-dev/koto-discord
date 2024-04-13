package dev.shiron.kotodiscord.command

import dev.shiron.kotodiscord.i18n.I18n
import dev.shiron.kotodiscord.util.data.action.BotSlashCommandData
import dev.shiron.kotodiscord.util.meta.SingleCommandEnum
import dev.shiron.kotodiscord.util.service.SingleCommandServiceClass
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class HelloService
    @Autowired
    constructor(
        private val i18n: I18n,
    ) : SingleCommandServiceClass(
            SingleCommandEnum.HELLO,
            i18n,
        ) {
        override fun onSlashCommand(cmd: BotSlashCommandData) {
            cmd.send(
                i18n.format(
                    "command.message.hello",
                    cmd.event.user.asMention,
                ),
            )
        }
    }
