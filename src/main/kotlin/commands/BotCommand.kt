package commands

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

abstract class BotCommand(val commandMeta: BotCommandMeta, private val sharedDefault: Boolean = false) :
    ListenerAdapter() {
    open val commandOptions: List<OptionData> = listOf()

    private val sharedOptionData by lazy {
        OptionData(
            OptionType.BOOLEAN,
            "shared",
            "他の人にコマンドの実行結果が見えるかどうか。デフォルト値は${if (sharedDefault) "true(見える)" else "false(見えない)"}"
        )
    }

    open val slashCommandData: SlashCommandData
        get() = Commands.slash(commandMeta.cmd, commandMeta.description).addOptions(commandOptions)
            .addOptions(sharedOptionData)

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name != commandMeta.cmd) return
        if (event.guild == null) {
            event.reply("このコマンドはサーバー内でのみ実行できます。").queue()
            return
        }

        event.deferReply().setEphemeral((event.getOption("shared")?.asBoolean?.not()) ?: sharedDefault.not()).queue()

        val eventData = BotSlashCommandEvent(
            event,
            event.guild!!,
            event.channel
        )

        onSlashCommand(eventData)
    }

    abstract fun onSlashCommand(event: BotSlashCommandEvent)
}

data class BotSlashCommandEvent(
    val event: SlashCommandInteractionEvent,
    val guild: Guild,
    val channel: Channel
) {
    fun replay(message: String) {
        event.hook.sendMessage(message).queue()
    }
}