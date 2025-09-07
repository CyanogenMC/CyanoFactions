package io.github.toberocat.improvedfactions.api.events

import io.github.toberocat.improvedfactions.factions.Faction
import io.github.toberocat.improvedfactions.user.FactionUser
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Fired when a player joins a faction.
 */
class FactionJoinEvent(val faction: Faction, val user: FactionUser) : Event(), Cancellable {
    private var isCancelled = false

    override fun getHandlers(): HandlerList = handlerList
    
    override fun isCancelled(): Boolean = isCancelled

    override fun setCancelled(cancel: Boolean) {
        isCancelled = cancel
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
