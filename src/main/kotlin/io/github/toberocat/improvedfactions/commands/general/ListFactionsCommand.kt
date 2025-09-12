package io.github.toberocat.improvedfactions.commands.general

import io.github.toberocat.improvedfactions.annotations.command.CommandCategory
import io.github.toberocat.improvedfactions.annotations.command.CommandResponse
import io.github.toberocat.improvedfactions.annotations.command.GeneratedCommandMeta
import io.github.toberocat.improvedfactions.commands.CommandProcessResult
import io.github.toberocat.improvedfactions.commands.sendCommandResult
import io.github.toberocat.improvedfactions.database.DatabaseManager.loggedTransaction
import io.github.toberocat.improvedfactions.factions.FactionHandler
import io.github.toberocat.improvedfactions.modules.relations.RelationsModule
import io.github.toberocat.improvedfactions.modules.relations.RelationsModule.allies
import io.github.toberocat.improvedfactions.modules.relations.RelationsModule.enemies
import io.github.toberocat.improvedfactions.translation.sendLocalized
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@GeneratedCommandMeta(
    label = "list",
    category = CommandCategory.GENERAL_CATEGORY,
    module = "base",
    responses = [
        CommandResponse("listHeader"),
        CommandResponse("listFaction"),
        CommandResponse("noFactions")
    ]
)
abstract class ListFactionsCommand : ListFactionsCommandContext() {

    fun process(sender: CommandSender): CommandProcessResult {
        sender.sendCommandResult(listHeader())

        val factions = loggedTransaction {
            FactionHandler.getFactions().map { faction ->
                val ownerName = Bukkit.getOfflinePlayer(faction.owner).name ?: "Unknown"
                val alliesCount = if (RelationsModule.isEnabled) faction.allies().size else 0
                val enemiesCount = if (RelationsModule.isEnabled) faction.enemies().size else 0
                val description = if (faction.description.isNotEmpty()) faction.description else "No description"
                
                listFaction(
                    "name" to faction.name,
                    "power" to faction.accumulatedPower.toString(),
                    "members" to faction.members().count().toString(),
                    "leader" to ownerName,
                    "allies" to alliesCount.toString(),
                    "wars" to enemiesCount.toString(),
                    "description" to description
                )
            }
        }

        if (factions.isEmpty()) return noFactions()

        factions.dropLast(1).forEach { sender.sendCommandResult(it) }
        return  factions.last()
    }
}