package io.github.toberocat.improvedfactions.commands.manage

import io.github.toberocat.improvedfactions.annotations.command.CommandCategory
import io.github.toberocat.improvedfactions.annotations.command.CommandResponse
import io.github.toberocat.improvedfactions.annotations.command.GeneratedCommandMeta
import io.github.toberocat.improvedfactions.commands.CommandProcessResult
import io.github.toberocat.improvedfactions.database.DatabaseManager.loggedTransaction
import io.github.toberocat.improvedfactions.modules.base.BaseModule
import io.github.toberocat.improvedfactions.permissions.Permissions
import io.github.toberocat.improvedfactions.user.factionUser
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@GeneratedCommandMeta(
    label = "setdesc",
    category = CommandCategory.MANAGE_CATEGORY,
    module = BaseModule.MODULE_NAME,
    responses = [
        CommandResponse("descriptionSet"),
        CommandResponse("factionNeeded"),
        CommandResponse("noPermission"),
        CommandResponse("descriptionTooLong")
    ]
)
abstract class DescriptionCommand : DescriptionCommandContext() {

    fun process(player: Player, description: String): CommandProcessResult {
        return setFactionDescription(player, description)
    }

    fun process(sender: CommandSender, target: Player, description: String): CommandProcessResult {
        return setFactionDescription(target, description)
    }

    private fun setFactionDescription(player: Player, description: String): CommandProcessResult {
        val factionUser = player.factionUser()
        val faction = factionUser.faction() ?: return factionNeeded()

        if (!factionUser.hasPermission(Permissions.EDIT_FACTION_INFO)) {
            return noPermission()
        }

        if (description.length > 255) {
            return descriptionTooLong("max" to "255")
        }

        loggedTransaction {
            faction.description = description
        }

        return descriptionSet("description" to description)
    }
}