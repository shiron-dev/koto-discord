package dev.shiron.kotodiscord.util.service

import dev.shiron.kotodiscord.i18n.I18n
import dev.shiron.kotodiscord.util.meta.SingleCommandEnum
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

abstract class SingleCommandServiceClass(
    val commandMeta: SingleCommandEnum,
    private val i18n: I18n,
) : RunnableCommandServiceClass(
        commandMeta.metadata,
        i18n,
    ) {
    open val slashCommandData: SlashCommandData
        get() =
            Commands.slash(
                runMeta.commandName,
                i18n.format("command.description.$commandName"),
            ).addOptions(commandOptions)
                .addOptions(sharedOptionData)

    override val commandName: String
        get() = commandMeta.metadata.commandName
}
