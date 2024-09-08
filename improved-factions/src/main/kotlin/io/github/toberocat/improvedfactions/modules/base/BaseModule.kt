package io.github.toberocat.improvedfactions.modules.base

import io.github.toberocat.improvedfactions.ImprovedFactionsPlugin
import io.github.toberocat.toberocore.command.CommandExecutor
import org.bukkit.OfflinePlayer

interface BaseModule {
    val moduleName: String

    fun onEnable(plugin: ImprovedFactionsPlugin) {}
    fun reloadConfig(plugin: ImprovedFactionsPlugin) {}
    fun addCommands(plugin: ImprovedFactionsPlugin, executor: CommandExecutor) {}
    fun onLoadDatabase(plugin: ImprovedFactionsPlugin) {}
    fun onPapiPlaceholder(placeholders: HashMap<String, (player: OfflinePlayer) -> String?>) {}

    fun shouldEnable(plugin: ImprovedFactionsPlugin) = plugin.config.getBoolean("modules.$moduleName")

    fun warn(message: String) {
        ImprovedFactionsPlugin.instance.logger.info("[$moduleName] $message")
    }
}