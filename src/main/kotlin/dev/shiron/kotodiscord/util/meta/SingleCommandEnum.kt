package dev.shiron.kotodiscord.util.meta

enum class SingleCommandEnum(val metadata: RunnableCommandMeta) {
    HELLO(RunnableCommandMeta("hello")),
    HELP(RunnableCommandMeta("help")),
    ABOUT(RunnableCommandMeta("about")),
    BUMP(RunnableCommandMeta("notify_bump")),
}
