package io.github.toberocat.improvedfactions.utils

import io.github.toberocat.improvedfactions.ImprovedFactionsPlugin
import io.github.toberocat.improvedfactions.charts.addFactionsChart
import io.github.toberocat.improvedfactions.charts.addModuleChart
import org.bstats.bukkit.Metrics

class BStatsCollector(plugin: ImprovedFactionsPlugin) {
    init {
        val metrics = Metrics(plugin, 27305)
        metrics.addModuleChart()
        metrics.addFactionsChart()
    }
}
