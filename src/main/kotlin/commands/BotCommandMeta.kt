package commands

data class CommandPath(val path: String) {
    val objs = path.split(".")
}

data class BotCommandMetaData(
    val cmd: String,
    val description: String,
    val commandPath: CommandPath,
    val subcommands: List<BotSubcommandMetaData> = listOf(),
) {
    init {
        for (cmd in subcommands) {
            cmd.parentCommand = this
        }
    }
}

data class BotSubcommandMetaData(
    val cmd: String,
    val description: String? = null,
) {
    lateinit var parentCommand: BotCommandMetaData

    val commandPath: CommandPath
        get() = CommandPath("${parentCommand.commandPath.path}.$cmd")
}

enum class BotCommandMeta(val meta: BotCommandMetaData) {
    HELLO(BotCommandMetaData("hello", "挨拶をします。", CommandPath("koto.util.hello"))),
    VC_NOTIFICATION(
        BotCommandMetaData(
            "notification",
            "VCの入退室通知をします。",
            CommandPath("koto.vc.notification"),
            listOf(
                BotSubcommandMeta.VC_NOTIFICATION_SET.meta,
                BotSubcommandMeta.VC_NOTIFICATION_GET.meta,
                BotSubcommandMeta.VC_NOTIFICATION_DELETE.meta,
            ),
        ),
    ),
}

enum class BotSubcommandMeta(val meta: BotSubcommandMetaData) {
    VC_NOTIFICATION_SET(
        BotSubcommandMetaData("set"),
    ),
    VC_NOTIFICATION_GET(
        BotSubcommandMetaData("get"),
    ),
    VC_NOTIFICATION_DELETE(
        BotSubcommandMetaData("delete"),
    ),
}
