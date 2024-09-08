package io.github.toberocat.improvedfactions.modules.chat

import io.github.toberocat.improvedfactions.ImprovedFactionsPlugin
import io.github.toberocat.improvedfactions.modules.base.BaseModule
import io.github.toberocat.improvedfactions.modules.chat.commands.ChatCommand
import io.github.toberocat.improvedfactions.modules.chat.handles.ChatModuleHandle
import io.github.toberocat.improvedfactions.modules.chat.handles.DummyChatModuleHandle
import io.github.toberocat.improvedfactions.modules.chat.impl.ChatModuleHandleImpl
import io.github.toberocat.improvedfactions.modules.chat.listener.ChatListener
import io.github.toberocat.toberocore.command.CommandExecutor
import org.bukkit.entity.Player

object ChatModule : BaseModule {
    const val MODULE_NAME = "chat"
    override val moduleName = MODULE_NAME

    var chatModuleHandle: ChatModuleHandle = DummyChatModuleHandle()

    override fun addCommands(plugin: ImprovedFactionsPlugin, executor: CommandExecutor) {
        executor.addChild(ChatCommand(chatModuleHandle, plugin))
    }

    override fun onEnable(plugin: ImprovedFactionsPlugin) {
        chatModuleHandle = ChatModuleHandleImpl()
        plugin.registerListeners(
            ChatListener(chatModuleHandle)
        )
    }

    fun Player.resetChatMode() = chatModuleHandle.setChatMode(uniqueId, ChatMode.GLOBAL)

    fun Player.sendFactionChat(message: String) = ChatMode.FACTION.sendIntoChat(this, message)

    fun Player.toggleChatMode(): ChatMode {
        val current = chatModuleHandle.getChatMode(uniqueId).ordinal
        val values = enumValues<ChatMode>()
        val nextChatMode = values[(current + 1) % values.size]
        chatModuleHandle.setChatMode(uniqueId, nextChatMode)
        return nextChatMode
    }

    fun chatPair() = MODULE_NAME to this
}