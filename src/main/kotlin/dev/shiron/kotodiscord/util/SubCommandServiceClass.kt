package dev.shiron.kotodiscord.util

import dev.shiron.kotodiscord.util.meta.SubCommandEnum
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.context.MessageSource
import java.util.*

abstract class SubCommandServiceClass(
    meta: SubCommandEnum,
    private val messages: MessageSource
) : RunnableCommandServiceClass(meta.meta, messages) {

    override val slashCommandData: SlashCommandData
        // TODO: group.name
        get() = Commands.slash(meta.name, messages.getMessage("command.description.${meta.name}", arrayOf(), Locale.JAPAN)).addOptions(commandOptions)
            .addOptions(sharedOptionData)
}
