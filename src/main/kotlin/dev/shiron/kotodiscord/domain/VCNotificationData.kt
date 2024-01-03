package dev.shiron.kotodiscord.domain

import jakarta.persistence.*

@Entity
@Table(name = "vc_notification")
data class VCNotificationData(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long = 0,
    val guildId: Long,
    val vcChannelId: Long?,
    val vcCategoryId: Long?,
    val textChannelId: Long
)
