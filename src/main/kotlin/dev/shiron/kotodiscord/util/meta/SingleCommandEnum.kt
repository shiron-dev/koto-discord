package dev.shiron.kotodiscord.util.meta

enum class SingleCommandEnum(val metadata: RunnableCommandMeta) {
    HELLO(RunnableCommandMeta("hello")),
    HELP(RunnableCommandMeta("help")),
    ABOUT(RunnableCommandMeta("about")),
    VC_NOTIFICATION(RunnableCommandMeta("vc_notification")),
    BUMP(RunnableCommandMeta("notify_bump")),
}
