package dev.shiron.kotodiscord

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KotoDiscordApplication

fun main(args: Array<String>) {
	runApplication<KotoDiscordApplication>(*args)
}
