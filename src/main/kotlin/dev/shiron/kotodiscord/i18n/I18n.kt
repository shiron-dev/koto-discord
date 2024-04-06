package dev.shiron.kotodiscord.i18n

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Component
import java.util.*

@Component
class I18n
    @Autowired
    constructor(private val messages: MessageSource) {
        fun format(
            key: String,
            vararg args: String,
        ): String {
            return messages.getMessage(key, args, Locale.JAPAN)
        }
    }
