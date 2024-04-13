package dev.shiron.kotodiscord.command

import dev.shiron.kotodiscord.i18n.I18n
import dev.shiron.kotodiscord.util.data.action.BotSlashCommandData
import dev.shiron.kotodiscord.util.meta.SingleCommandEnum
import dev.shiron.kotodiscord.util.service.SingleCommandServiceClass
import dev.shiron.kotodiscord.vars.properties.AppProperties
import dev.shiron.kotodiscord.vars.properties.DevelopProperties
import net.dv8tion.jda.api.interactions.components.buttons.Button
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest

@Service
class AboutService
    @Autowired
    constructor(
        private val appProperties: AppProperties,
        private val devProperties: DevelopProperties,
        private val i18n: I18n,
    ) : SingleCommandServiceClass(
            SingleCommandEnum.ABOUT,
            i18n,
        ) {
        private final var implementationVersion: String?
        val started = Date()

        init {
            val manifestStream = javaClass.getResourceAsStream("/META-INF/MANIFEST.MF")
            val manifest = Manifest(manifestStream)
            implementationVersion = manifest.mainAttributes.getValue("Implementation-Version")
        }

        override fun onSlashCommand(cmd: BotSlashCommandData) {
            val timeZoneJP = TimeZone.getTimeZone("Asia/Tokyo")
            val fmt = SimpleDateFormat()
            fmt.timeZone = timeZoneJP

            val now = Date()
            val diff = now.time - started.time
            val format = SimpleDateFormat("MM:dd HH:mm")
            format.timeZone = TimeZone.getTimeZone("UTC")
            val formattedTime = format.format(Date(diff))
            cmd.event.hook.sendMessage(
                i18n.format(
                    "command.message.about",
                    cmd.event.jda.selfUser.asMention,
                    implementationVersion ?: "",
                    cmd.event.jda.gatewayPing.toString(),
                    "${fmt.format(started)} ($formattedTime)",
                    if (devProperties.isDevMode) "\n:warning: 開発モード :warning:" else "",
                ),
            ).addActionRow(
                Button.link(
                    appProperties.inviteLink ?: "",
                    i18n.format("button.support"),
                ),
            ).queue()
        }
    }
