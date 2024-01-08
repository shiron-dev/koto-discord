package dev.shiron.kotodiscord.repository

import dev.shiron.kotodiscord.domain.VCNotificationData
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface VCNotificationDataRepository : CrudRepository<VCNotificationData, Long?> {
    fun findAllByGuildId(guildId: Long?): List<VCNotificationData>?
}
