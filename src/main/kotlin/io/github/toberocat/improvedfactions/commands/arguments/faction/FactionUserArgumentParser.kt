package io.github.toberocat.improvedfactions.commands.arguments.faction

import io.github.toberocat.improvedfactions.annotations.localization.Localization
import io.github.toberocat.improvedfactions.commands.arguments.ArgumentParser
import io.github.toberocat.improvedfactions.commands.arguments.ArgumentParsingException
import io.github.toberocat.improvedfactions.commands.arguments.ParsingContext
import io.github.toberocat.improvedfactions.user.FactionUser
import io.github.toberocat.improvedfactions.user.factionUser
import io.github.toberocat.improvedfactions.utils.getOfflinePlayerByName
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Localization("base.arguments.faction-user.not-player")
@Localization("base.arguments.faction-user.player-not-found")
@Localization("base.arguments.faction-user.not-faction-member")
class FactionUserArgumentParser : ArgumentParser {

    override fun parse(sender: CommandSender, arg: String, args: Array<String>): FactionUser {
        if (sender !is Player) {
            throw ArgumentParsingException("base.arguments.faction-user.not-player")
        }

        val offlinePlayer =
                arg.getOfflinePlayerByName()
                        ?: throw ArgumentParsingException(
                                "base.arguments.faction-user.player-not-found"
                        )

        val factionUser = offlinePlayer.factionUser()
        val senderFaction = sender.factionUser().faction()

        // Verify the target is in the same faction as the sender
        if (factionUser.faction()?.id != senderFaction?.id) {
            throw ArgumentParsingException("base.arguments.faction-user.not-faction-member")
        }

        return factionUser
    }

    override fun rawTabComplete(pCtx: ParsingContext): List<String> {
        val player = pCtx.player() ?: return emptyList()
        val faction = player.factionUser().faction() ?: return emptyList()

        // Return names of faction members (excluding the sender for kick commands)
        return faction.members().filter { it.uniqueId != player.uniqueId }.mapNotNull {
            it.offlinePlayer().name
        }
    }
}
