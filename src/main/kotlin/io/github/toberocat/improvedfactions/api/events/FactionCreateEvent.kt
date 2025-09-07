package io.github.toberocat.improvedfactions.api.events

import io.github.toberocat.improvedfactions.factions.Faction
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Fired when a new faction is created.
 */
class FactionCreateEvent(val faction: Faction) : Event(), Cancellable {
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
