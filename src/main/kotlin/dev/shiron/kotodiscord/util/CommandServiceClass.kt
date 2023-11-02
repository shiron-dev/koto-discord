package dev.shiron.kotodiscord.util

import dev.shiron.kotodiscord.util.service.BotServiceMeta
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.context.MessageSource
import java.util.*

abstract class CommandServiceClass(meta: BotServiceMeta, private val messages: MessageSource, val sharedDefault: Boolean = false) : BotServiceClass(meta) {

    open val commandOptions: List<OptionData> = listOf()

    private val sharedOptionData by lazy {
        OptionData(
            OptionType.BOOLEAN,
            "shared",
            messages.getMessage("command.option.share", arrayOf(if (sharedDefault) "true(見える)" else "false(見えない)"), Locale.JAPAN)
        )
    }

    open val slashCommandData: SlashCommandData
        get() = Commands.slash(meta.name.lowercase(), messages.getMessage("command.description.${meta.name.lowercase()}", arrayOf(), Locale.JAPAN)).addOptions(commandOptions)
            .addOptions(sharedOptionData)

    abstract fun onSlashCommand(cmd: BotSlashCommandData)
}
