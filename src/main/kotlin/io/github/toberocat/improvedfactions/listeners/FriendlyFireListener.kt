package io.github.toberocat.improvedfactions.listeners

import io.github.toberocat.improvedfactions.database.DatabaseManager.loggedTransaction
import io.github.toberocat.improvedfactions.modules.base.BaseModule
import io.github.toberocat.improvedfactions.user.factionUser
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class FriendlyFireListener : Listener {

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerDamagePlayer(event: EntityDamageByEntityEvent) {
        val damaged = event.entity as? Player ?: return
        val damager = event.damager as? Player ?: return

        if (damaged.world.name !in BaseModule.config.allowedWorlds) return

        loggedTransaction {
            val damagedUser = damaged.factionUser()
            val damagerUser = damager.factionUser()

            if (damagedUser.factionId == damagerUser.factionId) {
                val faction = damagerUser.faction()

                if (faction != null && !faction.friendlyfire) {
                    event.isCancelled = true
                }
            }
        }
    }
}
