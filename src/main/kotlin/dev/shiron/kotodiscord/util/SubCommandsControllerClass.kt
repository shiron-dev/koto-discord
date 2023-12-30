package dev.shiron.kotodiscord.util

import dev.shiron.kotodiscord.util.meta.BotSubCommandGroupMeta
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import org.springframework.context.MessageSource
import java.util.*

abstract class SubCommandsControllerClass(val meta: BotSubCommandGroupMeta, val subcommands: List<RunnableCommandServiceClass>, private val messages: MessageSource) {
    open val slashCommandData: SlashCommandData
        get() = Commands.slash(
            meta.name,
            messages.getMessage("command.description.${meta.name}", arrayOf(), Locale.JAPAN)
        )
            .addSubcommands(subcommands.map { toSubcommandData(it.slashCommandData) })

    fun onSlashCommand(cmd: BotSlashCommandData) {
        for (subCommand in subcommands) {
            if (subCommand.meta.name == cmd.event.subcommandName) {
                cmd.event
                    .deferReply()
                    .setEphemeral((cmd.event.getOption("shared")?.asBoolean?.not()) ?: subCommand.sharedDefault.not())
                    .queue()
                subCommand.onSlashCommand(cmd)
                return
            }
        }
        cmd.event.reply(messages.getMessage("command.error.notfound.sub", arrayOf(cmd.event.subcommandName), Locale.JAPAN)).queue()
    }
}

private fun toSubcommandData(slashCommandData: SlashCommandData): SubcommandData {
    return SubcommandData(slashCommandData.name, slashCommandData.description)
}
