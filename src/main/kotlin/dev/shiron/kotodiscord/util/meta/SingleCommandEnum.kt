package dev.shiron.kotodiscord.util.meta

enum class SingleCommandEnum(val metadata: RunnableCommandMeta) {
    HELLO(RunnableCommandMeta("hello")),
    HELP(RunnableCommandMeta("help"))
}
