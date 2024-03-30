package dev.shiron.kotodiscord.repository

import dev.shiron.kotodiscord.domain.BumpConfigData
import dev.shiron.kotodiscord.domain.BumpJobQueueData
import jakarta.transaction.Transactional
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime

interface BumpJobQueueDataRepository : CrudRepository<BumpJobQueueData, Long?> {
    fun findAllByExecAtBefore(date: LocalDateTime): List<BumpJobQueueData>?

    fun findByBumpConfig(config: BumpConfigData): BumpJobQueueData?

    @Transactional
    fun deleteByBumpConfig(config: BumpConfigData)
}
