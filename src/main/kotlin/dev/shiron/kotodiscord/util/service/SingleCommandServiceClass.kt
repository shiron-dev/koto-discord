package dev.shiron.kotodiscord.util.service

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
                    "command.description.$commandName",
                    arrayOf(),
                    Locale.JAPAN,
                ),
            ).addOptions(commandOptions)
                .addOptions(sharedOptionData)

    override val commandName: String
        get() = commandMeta.metadata.commandName
}
