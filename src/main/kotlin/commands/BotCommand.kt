package commands

import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

abstract class BotCommand : ListenerAdapter() {
    abstract val commandName: CommandName
    abstract val description: String

    open val slashCommandData: SlashCommandData = Commands.slash(commandName.name, description)

}