package dev.shiron.kotodiscord.service.command.vc.notify

import dev.shiron.kotodiscord.domain.VCNotificationData
import dev.shiron.kotodiscord.i18n.I18n
import dev.shiron.kotodiscord.util.data.action.*
import dev.shiron.kotodiscord.util.meta.SingleCommandEnum
import dev.shiron.kotodiscord.util.service.SingleCommandServiceClass
import net.dv8tion.jda.api.interactions.components.ActionComponent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class VCCommand
    @Autowired
    constructor(
        private val vcService: VCService,
        private val i18n: I18n,
    ) : SingleCommandServiceClass(
            SingleCommandEnum.VC_NOTIFICATION,
            i18n,
        ) {
        fun genBtnSet(shared: Boolean): Button {
            return Button.secondary(
                genComponentId("set", shared, ComponentSendType.EDIT),
                i18n.format("button.vc_notification.set"),
            )
        }

        fun genBtnSetAll(shared: Boolean): Button {
            return Button.secondary(
                genComponentId("set_all", shared, ComponentSendType.EDIT),
                i18n.format("button.vc_notification.set_all"),
            )
        }

        fun genBtnRemove(shared: Boolean): Button {
            return Button.secondary(
                genComponentId("remove", shared, ComponentSendType.EDIT),
                i18n.format("button.vc_notification.remove"),
            )
        }

        override fun onSlashCommand(cmd: BotSlashCommandData) {
            val dataList = vcService.listVCNotification(cmd.guild.idLong)

            var msg = i18n.format("command.message.vc_notification.main") + "\n\n"
            val componets = mutableListOf<ActionComponent>()

            if (dataList.isEmpty()) {
                msg += i18n.format("command.message.vc_notification.empty")
                componets.add(genBtnSetAll(cmd.shared))
                componets.add(genBtnSet(cmd.shared))
            } else {
                msg += i18n.format("command.message.vc_notification.exist") + "\n"

                if (dataList.size < 10) {
                    if (dataList.find { it.vcCategoryId == null && it.vcChannelId == null } == null) {
                        componets.add(genBtnSetAll(cmd.shared))
                    }

                    componets.add(genBtnSet(cmd.shared))
                } else {
                    msg += i18n.format("command.message.vc_notification.set.limit")
                }

                msg += dataList.joinToString("\n") { "- ${it.vcName} -> <#${it.textChannelId}>" }

                componets.add(genBtnRemove(cmd.shared))
            }

            cmd.send(msg, componets.ifEmpty { null })
        }

        override fun onButton(event: BotButtonData) {
            when (event.actionData.key) {
                "set" -> {
                    event.send(
                        i18n.format("command.message.vc_notification.set.vc"),
                        listOf(
                            EntitySelectMenu.create(
                                genComponentId("vc", event.actionData.isShow, ComponentSendType.EDIT),
                                EntitySelectMenu.SelectTarget.CHANNEL,
                            ).build(),
                        ),
                    )
                }
                "set_all" -> {
                    event.send(
                        i18n.format("command.message.vc_notification.set.text"),
                        listOf(
                            EntitySelectMenu.create(
                                genComponentId(
                                    "text",
                                    event.actionData.isShow,
                                    ComponentSendType.EDIT,
                                    null,
                                ),
                                EntitySelectMenu.SelectTarget.CHANNEL,
                            ).build(),
                        ),
                    )
                }
                "remove" -> {
                    val dataList = vcService.listVCNotification(event.guild.idLong)

                    val option =
                        StringSelectMenu.create(genComponentId("rm", event.actionData.isShow, ComponentSendType.EDIT)).apply {
                            dataList.forEachIndexed { index, vcNotificationData ->
                                val vcId = vcNotificationData.vcCategoryId ?: vcNotificationData.vcChannelId
                                val vcName = vcId.let { event.guild.channels.find { it.idLong == vcId }?.name }?.let { "#$it" } ?: "サーバー全体"
                                val textName = event.guild.channels.find { it.idLong == vcNotificationData.textChannelId }?.name?.let { "#$it" } ?: "不明"
                                addOption(
                                    "${index + 1} $vcName -> $textName",
                                    "${index + 1}",
                                )
                            }
                        }.build()
                    event.send(
                        i18n.format("command.message.vc_notification.remove"),
                        listOf(option),
                    )
                }
            }
        }

        override fun onEntitySelect(event: BotEntitySelectData) {
            when (event.actionData.key) {
                "vc" -> {
                    event.send(
                        i18n.format("command.message.vc_notification.set.text"),
                        listOf(
                            EntitySelectMenu.create(
                                genComponentId(
                                    "text",
                                    event.actionData.isShow,
                                    ComponentSendType.EDIT,
                                    event.values.first().idLong,
                                ),
                                EntitySelectMenu.SelectTarget.CHANNEL,
                            ).build(),
                        ),
                    )
                }
                "text" -> {
                    val vcChannelId = event.actionData.data
                    if (vcChannelId is Long?) {
                        val vcChannel = vcChannelId?.let { event.guild.getVoiceChannelById(it) }
                        val categoryChannel = vcChannelId?.let { event.guild.getCategoryById(it) }
                        val textChannel = event.guild.getTextChannelById(event.values.first().idLong)

                        if (vcChannelId != null && (vcChannel == null && categoryChannel == null)) {
                            event.send(i18n.format("command.message.vc_notification.set.error.vc"))
                            return
                        }
                        if (textChannel == null) {
                            event.send(i18n.format("command.message.vc_notification.set.error.text"))
                            return
                        }

                        vcService.setVCNotification(
                            VCNotificationData(
                                guildId = event.guild.idLong,
                                vcCategoryId = categoryChannel?.idLong,
                                vcChannelId = vcChannel?.idLong,
                                textChannelId = textChannel.idLong,
                            ),
                        )
                        event.send(
                            i18n.format(
                                "command.message.vc_notification.set.success",
                                vcChannel?.asMention ?: categoryChannel?.asMention ?: "サーバー全体",
                                textChannel.asMention,
                            ),
                        )
                    }
                }
            }
        }

        override fun onStringSelect(event: BotStringSelectData) {
            when (event.actionData.key) {
                "rm" -> {
                    val index = event.values.first().toIntOrNull()?.minus(1) ?: return
                    val data = vcService.listVCNotification(event.guild.idLong)[index]
                    vcService.removeVCNotification(data)
                    event.send("${index + 1} : ${data.vcName}  -> <#${data.textChannelId}>\n" + "の設定を削除しました")
                }
            }
        }
    }
