package dev.shiron.kotodiscord.bot

import dev.shiron.kotodiscord.AppProperties
import dev.shiron.kotodiscord.DevelopProperties
import dev.shiron.kotodiscord.NotificationProperties
import dev.shiron.kotodiscord.controller.CommandController
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Component
import java.util.*

@Component
class KotoMain
    @Autowired
    constructor(
        private val appProperties: AppProperties,
        private val notificationProperties: NotificationProperties,
        private val developProperties: DevelopProperties,
        private val commandController: CommandController,
        private val listeners: List<ListenerAdapter>,
        private val messages: MessageSource,
    ) {
        val jda: JDA by lazy {
            JDABuilder.createDefault(
                appProperties.token,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.MESSAGE_CONTENT,
            )
                .setRawEventsEnabled(true)
                .setActivity(
                    Activity.playing(
                        appProperties.activityMessage ?: "Koto Discord Bot",
                    ),
                )
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build()
        }

        fun start(): Boolean {
            jda.awaitReady()

            if (notificationProperties.isStartMessage) {
                val guild =
                    notificationProperties.guildID?.let {
                        jda.getGuildById(it)
                    } ?: return false
                val channel =
                    notificationProperties.channelID?.let {
                        guild.getTextChannelById(it)
                    }
                channel?.sendMessage(
                    messages.getMessage(
                        "notification.message.start",
                        arrayOf(
                            jda.selfUser.asMention,
                            Date().toString(),
                            Random(
                                System.currentTimeMillis(),
                            ).nextInt(100000, 1000000).toString(),
                        ),
                        Locale.JAPAN,
                    ),
                )?.queue()
            }

            if (developProperties.isDevMode) {
                val devGuild =
                    developProperties.devGuildID?.let {
                        jda.getGuildById(it)
                    } ?: return false
                register(devGuild)
            } else {
                register()
            }

            return true
        }

        private fun register(guild: Guild? = null) {
            val commands = commandController.getCommandsData().toMutableList()

            val commandListUpdateAction =
                guild?.updateCommands() ?: jda.updateCommands()

            commandListUpdateAction.addCommands(commands).queue()

            for (listener in listeners) {
                jda.addEventListener(listener)
            }
        }
    }
