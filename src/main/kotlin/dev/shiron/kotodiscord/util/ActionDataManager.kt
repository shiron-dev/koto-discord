package dev.shiron.kotodiscord.util

import dev.shiron.kotodiscord.util.data.BotActionData
import java.util.*

object ActionDataManager {
    private val actionData: MutableMap<String, BotActionData> = mutableMapOf()

    fun newActionData(data: BotActionData): String {
        var id = UUID.randomUUID().toString()
        while (actionData.containsKey(id)) {
            id = UUID.randomUUID().toString()
        }
        data.componentIdData.actionId = id
        actionData[id] = data

        return id
    }

    fun removeActionData(id: String) {
        actionData.remove(id)
    }

    operator fun get(id: String): BotActionData? {
        return actionData[id]
    }

    fun cleanByMin(min: Int) {
        val now = Date()
        val minTime = now.time - (min * 60 * 1000)
        actionData.filter { it.value.createAt.time < minTime }.forEach { actionData.remove(it.key) }
    }
}
