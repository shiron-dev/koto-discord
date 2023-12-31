package dev.shiron.kotodiscord.util

import dev.shiron.kotodiscord.util.data.BotActionData
import dev.shiron.kotodiscord.util.data.ComponentIdData
import dev.shiron.kotodiscord.util.meta.SubCommandEnum
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import org.springframework.context.MessageSource
import java.util.*

abstract class SubCommandServiceClass(
    val commandMeta: SubCommandEnum,
    private val messages: MessageSource,
) : RunnableCommandServiceClass(commandMeta.metadata, messages) {
    open val subcommandData: SubcommandData
        get() =
            SubcommandData(
                commandMeta.metadata.commandName,
                messages.getMessage(
                    "command.description.${commandMeta.group.metadata.commandName}.${commandMeta.metadata.commandName}",
                    arrayOf(),
                    Locale.JAPAN,
                ),
            ).addOptions(commandOptions)
                .addOptions(sharedOptionData)

    override fun getComponentId(key: String): String {
        return ActionDataManager.newActionData(
            BotActionData(
                isShow = true,
                key = key,
                componentIdData =
                    ComponentIdData(
                        "${commandMeta.group.metadata.commandName}.${commandMeta.metadata.commandName}",
                        null,
                    ),
            ),
        )
    }
}
