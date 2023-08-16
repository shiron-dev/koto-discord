package bot

import Environment
import commands.BotCommand
import commands.util.HelloCommand
import commands.vc.notification.VCNotificationSubcommandable
import listeners.BotListener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import javax.security.auth.login.LoginException

object Bot {
    private val commands = listOf<BotCommand>(HelloCommand(), VCNotificationSubcommandable())
    private val listeners = listOf<BotListener>()

    lateinit var jda: JDA

    init {
        try {
            jda = JDABuilder.createDefault(
                Environment.botToken,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.MESSAGE_CONTENT,
            )
                .setRawEventsEnabled(true)
                .setActivity(Activity.playing(Environment.activityMessage))
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build()

            jda.awaitReady()
        } catch (e: LoginException) {
            e.printStackTrace()
        }
    }

    fun start(): Boolean {
        if (Environment.isDevMode) {
            val devGuild = Environment.devGuildId?.let { jda.getGuildById(it) } ?: return false
            register(devGuild)
            val devChannel = Environment.devChannelId?.let { devGuild.getTextChannelById(it) }
            devChannel?.sendMessage("Started")?.queue()
        } else {
            register()
        }

        return true
    }

    private fun register(guild: Guild? = null) {
        for (command in commands) {
            jda.addEventListener(command)
        }
        for (listener in listeners) {
            jda.addEventListener(listener)
        }

        if (guild == null) {
            jda.updateCommands().addCommands(commands.map { it.slashCommandData }).queue()
        } else {
            guild.updateCommands().addCommands(commands.map { it.slashCommandData }).queue()
        }
    }
}
