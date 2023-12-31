package dev.shiron.kotodiscord.util.meta

enum class SubCommandGroupEnum(val metadata: BotSubCommandGroupMeta) {
    VC_NOTIFICATION(BotSubCommandGroupMeta("vc_notification"))
}

enum class SubCommandEnum(val metadata: RunnableCommandMeta, val group: SubCommandGroupEnum) {
    VC_NOTIFICATION_SET(RunnableCommandMeta("set"), SubCommandGroupEnum.VC_NOTIFICATION),
    VC_NOTIFICATION_REMOVE(RunnableCommandMeta("remove"), SubCommandGroupEnum.VC_NOTIFICATION),
    VC_NOTIFICATION_LIST(RunnableCommandMeta("list"), SubCommandGroupEnum.VC_NOTIFICATION)
}
