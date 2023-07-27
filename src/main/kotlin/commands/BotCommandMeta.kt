package commands

data class CommandPath(val path: String) {
    val objs = path.split(".")
}

enum class BotCommandMeta(val cmd: String, val commandPath: CommandPath, val description: String) {
    HELLO("hello", CommandPath("koto.util.hello"), "挨拶をします。")
}