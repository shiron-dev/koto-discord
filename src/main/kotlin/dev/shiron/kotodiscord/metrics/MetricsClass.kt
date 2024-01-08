package dev.shiron.kotodiscord.metrics

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import dev.shiron.kotodiscord.AppProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class MetricsClass
    @Autowired
    constructor(private val appProperties: AppProperties) {
        fun commandRun(history: CommandHistory) {
            try {
                BufferedWriter(
                    FileWriter(
                        File(appProperties.metricsDir, "run_history.log"),
                        true,
                    ),
                ).use { writer ->
                    val timeModule = JavaTimeModule()
                    timeModule.addDeserializer(
                        LocalDateTime::class.java,
                        LocalDateTimeDeserializer(
                            DateTimeFormatter.ISO_DATE_TIME,
                        ),
                    )
                    val str =
                        ObjectMapper().configure(
                            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                            false,
                        ).registerModules(
                            timeModule,
                        )
                            .writeValueAsString(history)
                    writer.write("$str\n")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
