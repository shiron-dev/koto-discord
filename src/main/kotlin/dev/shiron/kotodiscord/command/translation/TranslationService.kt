package dev.shiron.kotodiscord.command.bump

import dev.shiron.kotodiscord.command.translation.translateText
import dev.shiron.kotodiscord.vars.properties.AppProperties
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

enum class TranslationReaction(val deeplCode: String, val reactionEmoji: String) {
    JAPANESE("ja", "🇯🇵"),
    ENGLISH_US("en", "🇺🇸"),
    ENGLISH_UK("en", "🇬🇧"),
    FINNISH("fi", "🇫🇮"),
}

@Component
class TranslationService
    @Autowired
    constructor(
        private val appProperties: AppProperties,
    ) : ListenerAdapter() {
        override fun onMessageReactionAdd(event: MessageReactionAddEvent) =
            runBlocking {
                val reaction = event.reaction.emoji.name

                val transCode = TranslationReaction.values().find { it.reactionEmoji == reaction } ?: return@runBlocking
                val count = event.reaction.count

                if (count < 2) return@runBlocking

                val channel = event.guild.getTextChannelById(event.channel.id) ?: return@runBlocking
                val message = channel.retrieveMessageById(event.messageId).complete() ?: return@runBlocking

                val apiKey = appProperties.deeplApiKey ?: return@runBlocking
                val sourceText = message.contentRaw
                val targetLang = transCode.deeplCode

                try {
                    val translatedText = translateText(apiKey, sourceText, targetLang)
                    message.reply(translatedText).queue()
                } catch (e: Exception) {
                    println("Error occurred: ${e.message}")
                }
            }
    }
