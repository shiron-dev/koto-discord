package dev.shiron.kotodiscord.util

import dev.shiron.kotodiscord.util.meta.RunnableCommandMeta
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.context.MessageSource
import java.util.*

abstract class RunnableCommandServiceClass(meta: RunnableCommandMeta, private val messages: MessageSource, val sharedDefault: Boolean = false) : BotServiceClass(meta) {

    open val commandOptions: List<OptionData> = listOf()

    protected val sharedOptionData by lazy {
        OptionData(
            OptionType.BOOLEAN,
            "shared",
            messages.getMessage("command.option.share", arrayOf(if (sharedDefault) "true(見える)" else "false(見えない)"), Locale.JAPAN)
        )
    }

    open val slashCommandData: SlashCommandData
        get() = Commands.slash(meta.name, messages.getMessage("command.description.${meta.name}", arrayOf(), Locale.JAPAN)).addOptions(commandOptions)
            .addOptions(sharedOptionData)

    abstract fun onSlashCommand(cmd: BotSlashCommandData)

    open fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {}
}
