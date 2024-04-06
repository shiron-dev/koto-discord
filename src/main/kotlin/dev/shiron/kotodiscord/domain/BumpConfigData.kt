package dev.shiron.kotodiscord.domain

import jakarta.persistence.*

@Entity
@Table(name = "bump_config")
data class BumpConfigData(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long = 0,
    val guildId: Long,
    var channelId: Long,
    val mentionId: Long?,
)
