package commands

abstract class BotSubcommandableCommand(
    commandName: BotCommandMeta,
    sharedDefault: Boolean = false,
) :
    BotCommand(
        commandName,
        sharedDefault,
    )
