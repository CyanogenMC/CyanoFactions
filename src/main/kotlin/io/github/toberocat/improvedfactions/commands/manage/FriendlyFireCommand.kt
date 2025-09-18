package io.github.toberocat.improvedfactions.commands.manage

import io.github.toberocat.improvedfactions.annotations.command.CommandCategory
import io.github.toberocat.improvedfactions.annotations.command.CommandResponse
import io.github.toberocat.improvedfactions.annotations.command.GeneratedCommandMeta
import io.github.toberocat.improvedfactions.commands.CommandProcessResult
import io.github.toberocat.improvedfactions.permissions.Permissions
import io.github.toberocat.improvedfactions.user.factionUser
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@GeneratedCommandMeta(
        label = "friendlyfire",
        category = CommandCategory.MANAGE_CATEGORY,
        module = "base",
        responses =
                [
                        CommandResponse("friendlyfireEnabled"),
                        CommandResponse("friendlyfireDisabled"),
                        CommandResponse("notInFaction"),
                        CommandResponse("noPermission"),
                ]
)
abstract class FriendlyFireCommand : FriendlyFireCommandContext() {
    fun process(player: Player, enabled: Boolean): CommandProcessResult? {
        return setFriendlyFire(player, enabled)
    }

    fun process(
            sender: CommandSender,
            target: OfflinePlayer,
            enabled: Boolean
    ): CommandProcessResult? {
        return setFriendlyFire(target, enabled)
    }

    private fun setFriendlyFire(player: OfflinePlayer, enabled: Boolean): CommandProcessResult? {
        val factionUser = player.factionUser()
        val faction = factionUser.faction() ?: return notInFaction()

        if (!factionUser.hasPermission(Permissions.SET_FRIENDLYFIRE)) {
            return noPermission()
        }

        faction.friendlyfire = enabled
        return if (enabled) friendlyfireEnabled() else friendlyfireDisabled()
    }
}
