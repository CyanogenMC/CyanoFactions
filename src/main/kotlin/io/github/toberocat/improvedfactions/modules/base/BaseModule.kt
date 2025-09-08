package io.github.toberocat.improvedfactions.modules.base

import io.github.toberocat.improvedfactions.ImprovedFactionsPlugin
import io.github.toberocat.improvedfactions.annotations.papi.PapiPlaceholder
import io.github.toberocat.improvedfactions.claims.clustering.detector.ClaimClusterDetector
import io.github.toberocat.improvedfactions.claims.clustering.query.DatabaseClaimQueryProvider
import io.github.toberocat.improvedfactions.commands.processor.baseCommandProcessors
import io.github.toberocat.improvedfactions.config.ImprovedFactionsConfig
import io.github.toberocat.improvedfactions.database.DatabaseConnector
import io.github.toberocat.improvedfactions.integrations.Integrations
import io.github.toberocat.improvedfactions.listeners.PlayerJoinListener
import io.github.toberocat.improvedfactions.listeners.move.MoveListener
import io.github.toberocat.improvedfactions.modules.Module
import io.github.toberocat.improvedfactions.translation.updateLanguages
import io.github.toberocat.improvedfactions.user.factionUser
import io.github.toberocat.improvedfactions.utils.BStatsCollector
import io.github.toberocat.improvedfactions.utils.FileUtils
import io.github.toberocat.improvedfactions.utils.toOfflinePlayer
import java.util.logging.Logger
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.OfflinePlayer
import org.jetbrains.exposed.sql.Database

object BaseModule : Module {
    const val MODULE_NAME = "base"
    override val moduleName = MODULE_NAME
    override var isEnabled = false

    lateinit var adventure: BukkitAudiences
    lateinit var config: ImprovedFactionsConfig
    lateinit var database: Database
    lateinit var claimChunkClusters: ClaimClusterDetector
    lateinit var logger: Logger
    lateinit var plugin: ImprovedFactionsPlugin
    lateinit var integrations: Integrations

    override fun onEnable(plugin: ImprovedFactionsPlugin) {
        this.plugin = plugin
        logger = plugin.logger

        adventure = BukkitAudiences.create(plugin)
        claimChunkClusters = ClaimClusterDetector(DatabaseClaimQueryProvider())
        config = ImprovedFactionsConfig.createConfig(plugin)

        BStatsCollector(plugin)

        copyFolders()

        plugin.registerListeners(MoveListener(), PlayerJoinListener())

        integrations = Integrations(plugin)
        updateLanguages(plugin)
    }

    override fun shouldEnable(plugin: ImprovedFactionsPlugin) = true

    override fun onEverythingEnabled(plugin: ImprovedFactionsPlugin) {
        claimChunkClusters.detectClusters()
        integrations.loadIntegrations()
    }

    override fun onLoadDatabase(plugin: ImprovedFactionsPlugin) {
        database = DatabaseConnector(plugin).createDatabase()
    }

    override fun onDisable(plugin: ImprovedFactionsPlugin) {
        adventure.close()
    }

    @PapiPlaceholder("owner", MODULE_NAME, "The owner of the faction")
    @PapiPlaceholder("name", MODULE_NAME, "The name of the faction")
    @PapiPlaceholder("rank", MODULE_NAME, "The rank of the player in the faction")
    @PapiPlaceholder("join_mode", MODULE_NAME, "The join mode of the faction")
    @PapiPlaceholder(
            "in_faction",
            MODULE_NAME,
            "Returns true if the player is in a faction, false otherwise"
    )
    override fun onPlaceholder(placeholders: HashMap<String, (player: OfflinePlayer) -> String?>) {
        placeholders["owner"] = { it.factionUser().faction()?.owner?.toOfflinePlayer()?.name }
        placeholders["name"] = { it.factionUser().faction()?.name }
        placeholders["rank"] = { it.factionUser().rank().name }
        placeholders["join_mode"] = { it.factionUser().faction()?.factionJoinType?.toString() }
        placeholders["in_faction"] = { it.factionUser().isInFaction().toString() }
    }

    private fun copyFolders() {
        FileUtils.copyAll(plugin, "languages")
    }

    override fun reloadConfig(plugin: ImprovedFactionsPlugin) {
        config.reload(plugin, plugin.config)
    }

    override fun getCommandProcessors(plugin: ImprovedFactionsPlugin) =
            baseCommandProcessors(plugin)

    fun basePair() = MODULE_NAME to this
}
