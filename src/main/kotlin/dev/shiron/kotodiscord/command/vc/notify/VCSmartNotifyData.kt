package dev.shiron.kotodiscord.command.vc.notify

import dev.shiron.kotodiscord.domain.VCNotificationData
import net.dv8tion.jda.api.entities.Message
import java.time.LocalDateTime

data class VCSmartNotifyData(
    val configData: VCNotificationData,
    val message: Message,
    val startDate: LocalDateTime,
)
