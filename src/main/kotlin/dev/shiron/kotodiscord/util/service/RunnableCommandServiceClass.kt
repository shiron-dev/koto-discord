package dev.shiron.kotodiscord.util.service

import dev.shiron.kotodiscord.i18n.I18n
import dev.shiron.kotodiscord.util.data.action.*
import dev.shiron.kotodiscord.util.meta.RunnableCommandMeta
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

abstract class RunnableCommandServiceClass(
    val runMeta: RunnableCommandMeta,
    private val i18n: I18n,
    val sharedDefault: Boolean = false,
) : BotServiceClass(
        runMeta,
    ) {
    open val commandOptions: List<OptionData> = listOf()

    protected val sharedOptionData by lazy {
        OptionData(
            OptionType.BOOLEAN,
            "shared",
            i18n.format(
                "command.option.share",
                if (sharedDefault) "true(見える)" else "false(見えない)",
            ),
        )
    }

    abstract val commandName: String

    fun genComponentId(
        key: String,
        shared: Boolean,
        componentReplayType: ComponentReplayType,
        data: Any? = null,
    ): String {
        return ActionDataManager.newActionData(
            BotActionData(
                isShow = shared,
                key = key,
                componentIdData =
                    ComponentIdData(
                        commandName,
                        null,
                    ),
                componentReplayType = componentReplayType,
                data = data,
            ),
        )
    }

    abstract fun onSlashCommand(cmd: BotSlashCommandData)

    open fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {}

    open fun onButton(event: BotButtonData) {}

    open fun onStringSelect(event: BotStringSelectData) {}

    open fun onEntitySelect(event: BotEntitySelectData) {}
}
