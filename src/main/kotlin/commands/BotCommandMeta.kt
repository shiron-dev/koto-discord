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
}
