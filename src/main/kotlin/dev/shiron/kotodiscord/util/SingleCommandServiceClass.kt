package dev.shiron.kotodiscord.util

import dev.shiron.kotodiscord.util.meta.SingleCommandEnum
import org.springframework.context.MessageSource

abstract class SingleCommandServiceClass(val commandMeta: SingleCommandEnum, messages: MessageSource) : RunnableCommandServiceClass(commandMeta.metadata, messages)
