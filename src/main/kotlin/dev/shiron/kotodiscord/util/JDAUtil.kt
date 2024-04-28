package dev.shiron.kotodiscord.util

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.IMentionable

fun getMentionable(
    guild: Guild,
    mentionId: Long?,
): IMentionable? {
    return mentionId?.let { guild.getMemberById(it) ?: guild.getRoleById(it) }
}
