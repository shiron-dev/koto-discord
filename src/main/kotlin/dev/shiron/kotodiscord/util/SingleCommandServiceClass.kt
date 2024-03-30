package dev.shiron.kotodiscord.util

import dev.shiron.kotodiscord.util.data.BotActionData
import dev.shiron.kotodiscord.util.data.ComponentIdData
import dev.shiron.kotodiscord.util.data.ComponentReplayType
import dev.shiron.kotodiscord.util.meta.SingleCommandEnum
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.context.MessageSource
import java.util.*

abstract class SingleCommandServiceClass(val commandMeta: SingleCommandEnum, private val messages: MessageSource) : RunnableCommandServiceClass(
    commandMeta.metadata,
    messages,
) {
    open val slashCommandData: SlashCommandData
        get() =
            Commands.slash(
                runMeta.commandName,
                messages.getMessage(
                    "command.description.${runMeta.commandName}",
                    arrayOf(),
                    Locale.JAPAN,
                ),
            ).addOptions(commandOptions)
                .addOptions(sharedOptionData)

    override fun getComponentId(
        key: String,
        componentReplayType: ComponentReplayType,
    ): String {
        return ActionDataManager.newActionData(
            BotActionData(
                isShow = true,
                key = key,
                componentIdData =
                    ComponentIdData(
                        runMeta.commandName,
                        null,
                    ),
                componentReplayType = componentReplayType,
            ),
        )
    }
}
