package dev.shiron.kotodiscord.util

import dev.shiron.kotodiscord.util.meta.BotServiceMeta
import net.dv8tion.jda.api.hooks.ListenerAdapter

abstract class BotServiceClass(
    val serviceMeta: BotServiceMeta,
) : ListenerAdapter()
