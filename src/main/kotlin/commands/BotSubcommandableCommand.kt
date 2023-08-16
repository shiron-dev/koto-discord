package commands

import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

abstract class BotSubcommandableCommand(
    commandMeta: BotCommandMeta,
    sharedDefault: Boolean = false,
) :
    BotCommand(
        commandMeta,
        sharedDefault,
    ) {

    abstract val subcommands: List<BotSubcommand>

    override val slashCommandData: SlashCommandData
        get() = Commands.slash(commandMeta.meta.cmd, commandMeta.meta.description)
            .addOptions(commandOptions)
            .addSubcommands(
                subcommands.map {
                    it.getSubcommandData(commandMeta.meta.description).addOptions(sharedOptionData)
                },
            )

    override fun onSlashCommand(event: BotSlashCommandEvent) {
        for (sub in subcommands) {
            if (event.event.subcommandName == sub.commandMeta.meta.cmd) {
                sub.onSubcommand(event)
                return
            }
        }
        onNotHasSubcommand(event)
    }

    open fun onNotHasSubcommand(event: BotSlashCommandEvent) {
        event.reply("`/${commandMeta.meta.cmd}`にはサブコマンド`${event.event.subcommandName}`が存在しません。")
    }
}

abstract class BotSubcommand(
    val commandMeta: BotSubcommandMeta,
) {

    fun getSubcommandData(defaultDescription: String) = SubcommandData(
        commandMeta.meta.cmd,
        commandMeta.meta.description ?: defaultDescription,
    ).addOptions(commandOptions)

    open val commandOptions: List<OptionData> = listOf()

    abstract fun onSubcommand(event: BotSlashCommandEvent)
}
