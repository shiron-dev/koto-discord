import io.github.cdimascio.dotenv.dotenv

object Environment {
    private val dotenv = dotenv()

    private fun toEnvBoolean(value: String?): Boolean {
        return value in listOf("True", "true", "TRUE")
    }

    val botToken: String? = dotenv["TOKEN"]
    val activityMessage: String = dotenv["ACTIVITY_MESSAGE"] ?: "Koto | /help"

    val isDevMode = toEnvBoolean(dotenv["DEV_FLAG"])
    val devGuildId: String? = dotenv["DEV_GUILD"]
    val devChannelId: String? = dotenv["DEV_CHANNEL"]
}
