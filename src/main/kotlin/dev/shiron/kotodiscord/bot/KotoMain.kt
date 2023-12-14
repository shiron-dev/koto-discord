package dev.shiron.kotodiscord.bot

import dev.shiron.kotodiscord.AppProperties
import dev.shiron.kotodiscord.DevelopProperties
import dev.shiron.kotodiscord.NotificationProperties
import dev.shiron.kotodiscord.controller.CommandController
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Component
import java.util.*

@Component
class KotoMain @Autowired constructor(
    private val appProperties: AppProperties,
    private val notificationProperties: NotificationProperties,
    private val developProperties: DevelopProperties,
    private val messages: MessageSource,
    private val commandController: CommandController
) {
    val jda: JDA by lazy {
        JDABuilder.createDefault(
            appProperties.token,
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_VOICE_STATES,
            GatewayIntent.MESSAGE_CONTENT
        )
            .setRawEventsEnabled(true)
            .setActivity(Activity.playing(appProperties.activityMessage ?: "Koto Discord Bot"))
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .build()
    }

    fun start(): Boolean {
        jda.awaitReady()

        if (notificationProperties.isStartMessage) {
            val guild = notificationProperties.guildID?.let { jda.getGuildById(it) } ?: return false
            val channel = notificationProperties.channelID?.let { guild.getTextChannelById(it) }
            channel?.sendMessage(
                messages.getMessage(
                    "notification.message.start",
                    arrayOf(
                        jda.selfUser.asMention,
                        Date().toString(),
                        Random(System.currentTimeMillis()).nextInt(100000, 1000000).toString()
                    ),
                    Locale.JAPAN
                )
            )?.queue()
        }

        if (developProperties.isDevMode) {
            val devGuild = developProperties.devGuildID?.let { jda.getGuildById(it) } ?: return false
            register(devGuild)
        } else {
            register()
        }

        return true
    }

    private fun register(guild: Guild? = null) {
        val commands = commandController.getCommandsData().toMutableList()

        val retrieveCommandsAction = guild?.retrieveCommands() ?: jda.retrieveCommands()
        val commandListUpdateAction = guild?.updateCommands() ?: jda.updateCommands()
        val deleteCommandById = { id: String -> guild?.deleteCommandById(id) ?: jda.deleteCommandById(id) }

        retrieveCommandsAction.queue {
            val rmCommand = it.toMutableList()

            for (command in commands.toList()) {
                val include = run {
                    for (retCmd in it) {
                        if (equalsCommand(command, retCmd)) {
                            return@run retCmd
                        }
                    }
                    null
                }
                if (include !== null) {
                    // 含まれている
                    commands.remove(command)
                    rmCommand.remove(include)
                }
            }

            for (cmd in rmCommand) {
                deleteCommandById(cmd.id).queue()
            }
            commandListUpdateAction.addCommands(commands).queue()
        }

        jda.addEventListener(commandController)
    }
}

private fun equalsCommand(a: SlashCommandData, b: Command): Boolean {
    return a.name == b.name && a.description == b.description && a.options.size == b.options.size && (
        a.options.isEmpty() || a.options.filterIndexed { index, optionData ->
            run {
                val aa = optionData
                val bb = b.options[index]
                return@run aa.name != bb.name || aa.description != bb.description || aa.type != bb.type || aa.isRequired != bb.isRequired
            }
        }.isEmpty()
        ) && a.subcommands.size == b.subcommands.size && (
        a.subcommands.isEmpty() || a.subcommands.filterIndexed { index, subcommandData ->
            run {
                val aa = subcommandData
                val bb = b.subcommands[index]
                println(aa.name)
                println(bb.name)
                return@run aa.name != bb.name || aa.description != bb.description || aa.options != bb.options
            }
        }.isEmpty()
        )
}
