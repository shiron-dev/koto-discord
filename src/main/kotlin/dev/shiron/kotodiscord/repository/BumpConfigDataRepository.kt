package dev.shiron.kotodiscord.repository

import dev.shiron.kotodiscord.domain.BumpConfigData
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BumpConfigDataRepository : CrudRepository<BumpConfigData, Long?> {
    fun findByGuildId(guildId: Long?): BumpConfigData?
}
