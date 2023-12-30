package dev.shiron.kotodiscord.util.meta

enum class SubCommandGroupEnum(val meta: BotSubCommandGroupMeta) {
    VC_NOTIFICATION(BotSubCommandGroupMeta("vc_notification"))
}

enum class SubCommandEnum(val meta: RunnableCommandMeta) {
    VC_NOTIFICATION_SET(RunnableCommandMeta("set")),
    VC_NOTIFICATION_REMOVE(RunnableCommandMeta("remove")),
    VC_NOTIFICATION_LIST(RunnableCommandMeta("list"))
}
