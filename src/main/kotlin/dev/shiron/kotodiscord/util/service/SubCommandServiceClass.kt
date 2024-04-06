package dev.shiron.kotodiscord.util.service

import dev.shiron.kotodiscord.i18n.I18n
import dev.shiron.kotodiscord.util.meta.SubCommandEnum
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

abstract class SubCommandServiceClass(
    val commandMeta: SubCommandEnum,
    private val i18n: I18n,
) : RunnableCommandServiceClass(commandMeta.metadata, i18n) {
    open val subcommandData: SubcommandData
        get() =
            SubcommandData(
                commandMeta.metadata.commandName,
                i18n.format("command.description.$commandName"),
            ).addOptions(commandOptions)
                .addOptions(sharedOptionData)

    override val commandName: String
        get() = "${commandMeta.group.metadata.commandName}.${commandMeta.metadata.commandName}"
}
