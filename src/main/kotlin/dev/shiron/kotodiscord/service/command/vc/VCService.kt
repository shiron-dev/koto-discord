package dev.shiron.kotodiscord.service.command.vc

import dev.shiron.kotodiscord.domain.VCNotificationData
import dev.shiron.kotodiscord.repository.VCNotificationDataRepository
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class VCService
    @Autowired
    constructor(
        private val vcRepository: VCNotificationDataRepository,
    ) : ListenerAdapter() {
        fun setVCNotification(data: VCNotificationData): VCNotificationData {
            return vcRepository.save(data)
        }

        fun removeVCNotification(data: VCNotificationData) {
            vcRepository.delete(data)
        }

        fun listVCNotification(guildId: Long): List<VCNotificationData> {
            return vcRepository.findAllByGuildId(guildId) ?: emptyList()
        }
    }
