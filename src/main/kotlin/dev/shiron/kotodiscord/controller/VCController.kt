package dev.shiron.kotodiscord.controller

import dev.shiron.kotodiscord.service.command.vc.ListCommand
import dev.shiron.kotodiscord.service.command.vc.RemoveCommand
import dev.shiron.kotodiscord.service.command.vc.SetCommand
import dev.shiron.kotodiscord.util.SubCommandsControllerClass
import dev.shiron.kotodiscord.util.meta.SubCommandGroupEnum
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Controller

@Controller
class VCController @Autowired constructor(
    setCommand: SetCommand,
    listCommand: ListCommand,
    removeCommand: RemoveCommand,
    messages: MessageSource
) : SubCommandsControllerClass(SubCommandGroupEnum.VC_NOTIFICATION.meta, listOf(setCommand, listCommand, removeCommand), messages)
