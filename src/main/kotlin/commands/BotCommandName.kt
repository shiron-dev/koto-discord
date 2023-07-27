package commands

data class CommandPath(val path: String) {
    val objs = path.split(".")
}

enum class CommandName(val cmd: String, val commandPath: CommandPath) {
    // HELLO("hello", CommandPath("koto.util.hello"))
}