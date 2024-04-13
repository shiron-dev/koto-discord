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
    val textChannelId: Long,
    @Column(columnDefinition = "boolean default true")
    val isSmart: Boolean = true,
    val mentionId: Long?,
) {
    val vcName: String
        get() =
            run {
                val vcId = vcCategoryId ?: vcChannelId
                return if (vcId == null) "`サーバー全体`" else "<#$vcId>"
            }
}
