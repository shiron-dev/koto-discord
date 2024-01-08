package dev.shiron.kotodiscord

import dev.shiron.kotodiscord.bot.KotoMain
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
class KotoDiscordApplication : CommandLineRunner {
    @Autowired
    private lateinit var kotoMain: KotoMain

    override fun run(vararg args: String?) {
        kotoMain.start()
    }
}

fun main(args: Array<String>) {
    runApplication<KotoDiscordApplication>(*args)
}
