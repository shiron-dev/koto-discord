package dev.shiron.kotodiscord.util.data

import java.util.Date

data class BotActionData(
    val isShow: Boolean,
    val key: String,
    val componentIdData: ComponentIdData,
) {
    val createAt: Date = Date()
}

data class ComponentIdData(
    val commandName: String,
    var actionId: String?,
) {
    val componentId: String
        get() = "${commandName}_$actionId"
}
