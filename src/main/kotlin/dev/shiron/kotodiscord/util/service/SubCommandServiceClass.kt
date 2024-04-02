package dev.shiron.kotodiscord.util.service

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
                    "command.description.$commandName",
                    arrayOf(),
                    Locale.JAPAN,
                ),
            ).addOptions(commandOptions)
                .addOptions(sharedOptionData)

    override val commandName: String
        get() = "${commandMeta.group.metadata.commandName}.${commandMeta.metadata.commandName}"

}
