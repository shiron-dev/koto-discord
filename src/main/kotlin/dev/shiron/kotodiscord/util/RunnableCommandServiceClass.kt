package dev.shiron.kotodiscord.util

import dev.shiron.kotodiscord.util.data.action.*
import dev.shiron.kotodiscord.util.meta.RunnableCommandMeta
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.springframework.context.MessageSource
import java.util.*

abstract class RunnableCommandServiceClass(
    val runMeta: RunnableCommandMeta,
    private val messages: MessageSource,
    val sharedDefault: Boolean = false,
) : BotServiceClass(
        runMeta,
    ) {
    open val commandOptions: List<OptionData> = listOf()

    protected val sharedOptionData by lazy {
        OptionData(
            OptionType.BOOLEAN,
            "shared",
            messages.getMessage(
                "command.option.share",
                arrayOf(if (sharedDefault) "true(見える)" else "false(見えない)"),
                Locale.JAPAN,
            ),
        )
    }

    abstract fun onSlashCommand(cmd: BotSlashCommandData)

    open fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {}

    open fun onButton(event: BotButtonData) {}

    open fun onStringSelect(event: BotStringSelectData) {}

    open fun onEntitySelect(event: BotEntitySelectData) {}

    abstract fun getComponentId(
        key: String,
        componentReplayType: ComponentReplayType = ComponentReplayType.REPLAY,
    ): String
}
