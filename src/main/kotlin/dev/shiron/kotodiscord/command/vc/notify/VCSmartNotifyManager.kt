package dev.shiron.kotodiscord.command.vc.notify

import dev.shiron.kotodiscord.domain.VCNotificationData

object VCSmartNotifyManager {
    private val smartNotifyData: MutableMap<VCNotificationData, VCSmartNotifyData> = mutableMapOf()

    fun new(data: VCSmartNotifyData) {
        smartNotifyData[data.configData] = data
    }

    fun remove(data: VCNotificationData) {
        smartNotifyData.remove(data)
    }

    operator fun get(data: VCNotificationData): VCSmartNotifyData? {
        return smartNotifyData[data]
    }
}
