package dev.shiron.kotodiscord.util.meta

enum class SubCommandGroupEnum(val metadata: BotSubCommandGroupMeta)

enum class SubCommandEnum(val metadata: RunnableCommandMeta, val group: SubCommandGroupEnum)
