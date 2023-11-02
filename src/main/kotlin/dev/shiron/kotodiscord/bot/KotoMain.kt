package dev.shiron.kotodiscord.bot

import dev.shiron.kotodiscord.AppProperties
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class KotoMain @Autowired constructor(
    private val appProperties: AppProperties
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
        return true
    }
}
