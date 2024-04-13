package dev.shiron.kotodiscord.command.vc.notify

import dev.shiron.kotodiscord.domain.VCNotificationData
import dev.shiron.kotodiscord.i18n.I18n
import dev.shiron.kotodiscord.util.data.action.*
import dev.shiron.kotodiscord.util.meta.SingleCommandEnum
import dev.shiron.kotodiscord.util.service.SingleCommandServiceClass
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
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

        fun genBtnSetSmart(shared: Boolean): Button {
            return Button.secondary(
                genComponentId("set_smart", shared, ComponentSendType.EDIT),
                i18n.format("button.vc_notification.set_smart"),
            )
        }

        override fun onSlashCommand(cmd: BotSlashCommandData) {
            val dataList = vcService.listVCNotification(cmd.guild.idLong)

            var msg = i18n.format("command.message.vc_notification.main") + "\n\n"
            val components = mutableListOf<ActionComponent>()

            if (dataList.isEmpty()) {
                msg += i18n.format("command.message.vc_notification.empty")
                components.add(genBtnSetAll(cmd.shared))
                components.add(genBtnSet(cmd.shared))
            } else {
                msg += i18n.format("command.message.vc_notification.exist") + "\n"

                if (dataList.size < 10) {
                    if (dataList.find { it.vcCategoryId == null && it.vcChannelId == null && it.textChannelId == cmd.event.channel.idLong } == null) {
                        components.add(genBtnSetAll(cmd.shared))
                    }

                    components.add(genBtnSet(cmd.shared))
                } else {
                    msg += i18n.format("command.message.vc_notification.set.limit")
                }

                msg +=
                    dataList.joinToString("\n") {
                        "- ${it.vcName} -> <#${it.textChannelId}> ${
                            if (it.isSmart) {
                                i18n.format("command.message.vc_notification.set_smart.true")
                            } else {
                                i18n.format("command.message.vc_notification.set_smart.false")
                            }
                        }"
                    }

                components.add(genBtnSetSmart(cmd.shared))
                components.add(genBtnRemove(cmd.shared))
            }

            cmd.send(msg, components.ifEmpty { null })
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
                    setVCNotification(event.guild, null, event.event.channel, event)
                }
                "set_smart" -> {
                    val dataList = vcService.listVCNotification(event.guild.idLong)

                    event.send(
                        i18n.format("command.message.vc_notification.set_smart"),
                        listOf(buildOptionSelection("set_smart", dataList, event.guild, event.actionData.isShow)),
                    )
                }
                "remove" -> {
                    val dataList = vcService.listVCNotification(event.guild.idLong)

                    event.send(
                        i18n.format("command.message.vc_notification.remove"),
                        listOf(buildOptionSelection("rm", dataList, event.guild, event.actionData.isShow)),
                    )
                }
            }
        }

        override fun onEntitySelect(event: BotEntitySelectData) {
            when (event.actionData.key) {
                "vc" -> {
                    val vcChannelId = event.event.values.first().idLong
                    setVCNotification(event.guild, vcChannelId, event.event.channel, event)
                }
            }
        }

        override fun onStringSelect(event: BotStringSelectData) {
            when (event.actionData.key) {
                "rm" -> {
                    val index = event.values.first().toIntOrNull()?.minus(1) ?: return
                    val data = vcService.listVCNotification(event.guild.idLong)[index]
                    vcService.removeVCNotification(data)
                    event.send(
                        i18n.format(
                            "command.message.vc_notification.remove.success",
                            (index + 1).toString(),
                            data.vcName,
                            "<#${data.textChannelId}>",
                        ),
                    )
                }
                "set_smart" -> {
                    val index = event.values.first().toIntOrNull()?.minus(1) ?: return
                    val data = vcService.listVCNotification(event.guild.idLong)[index]
                    vcService.setVCNotification(data.copy(isSmart = !data.isSmart))
                    event.send(
                        i18n.format(
                            "command.message.vc_notification.set_smart.success",
                            (index + 1).toString(),
                            data.vcName,
                            "<#${data.textChannelId}>",
                            if (!data.isSmart) {
                                i18n.format("command.message.vc_notification.set_smart.true")
                            } else {
                                i18n.format("command.message.vc_notification.set_smart.false")
                            },
                        ),
                    )
                }
            }
        }

        private fun setVCNotification(
            guild: Guild,
            vcChannelId: Long?,
            textChannel: MessageChannelUnion,
            event: BotSendEventClass,
        ) {
            val vcChannel = vcChannelId?.let { guild.getVoiceChannelById(it) }
            val categoryChannel = vcChannelId?.let { guild.getCategoryById(it) }

            if (vcChannelId != null && (vcChannel == null && categoryChannel == null)) {
                event.send(i18n.format("command.message.vc_notification.set.error.vc"))
                return
            }
            if (textChannel.type != ChannelType.TEXT) {
                event.send(i18n.format("command.message.vc_notification.set.error.text"))
                return
            }

            vcService.setVCNotification(
                VCNotificationData(
                    guildId = guild.idLong,
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

        private fun buildOptionSelection(
            key: String,
            dataList: List<VCNotificationData>,
            guild: Guild,
            isShow: Boolean,
        ): StringSelectMenu {
            return StringSelectMenu.create(genComponentId(key, isShow, ComponentSendType.EDIT)).apply {
                dataList.forEachIndexed { index, vcNotificationData ->
                    val vcId = vcNotificationData.vcCategoryId ?: vcNotificationData.vcChannelId
                    val vcName = vcId.let { guild.channels.find { it.idLong == vcId }?.name }?.let { "#$it" } ?: "サーバー全体"
                    val textName = guild.channels.find { it.idLong == vcNotificationData.textChannelId }?.name?.let { "#$it" } ?: "不明"
                    addOption(
                        "${index + 1} $vcName -> $textName ${
                            if (vcNotificationData.isSmart) {
                                i18n.format("command.message.vc_notification.set_smart.true")
                            } else {
                                i18n.format("command.message.vc_notification.set_smart.false")
                            }
                        }",
                        "${index + 1}",
                    )
                }
            }.build()
        }
    }
