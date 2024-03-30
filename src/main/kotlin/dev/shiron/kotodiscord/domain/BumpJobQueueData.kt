package dev.shiron.kotodiscord.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import java.time.LocalDateTime

@Entity
@Table(name = "bump_job_queue")
data class BumpJobQueueData(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long = 0,
    @OneToOne
    val bumpConfig: BumpConfigData,
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createAt: LocalDateTime,
    val execAt: LocalDateTime,
)
