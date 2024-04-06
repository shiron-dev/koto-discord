package dev.shiron.kotodiscord.util.data.action

import java.util.Date

enum class ComponentReplayType {
    REPLAY,
    EDIT,
    DEFER,
}

data class BotActionData(
    val isShow: Boolean,
    val key: String,
    val componentIdData: ComponentIdData,
    val componentReplayType: ComponentReplayType,
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
