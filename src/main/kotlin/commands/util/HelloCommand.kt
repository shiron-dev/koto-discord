package commands.util

import commands.BotCommand
import commands.BotCommandMeta
import commands.BotSlashCommandEvent

class HelloCommand : BotCommand(BotCommandMeta.HELLO) {
    override fun onSlashCommand(event: BotSlashCommandEvent) {
        event.replay("${if (event.event.getOption("shared")?.asBoolean == true) "みなさん、" else ""}こんにちは！")
    }
}