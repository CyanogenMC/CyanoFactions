package io.github.toberocat.improvedfactions.api

import io.github.toberocat.improvedfactions.factions.Faction
import io.github.toberocat.improvedfactions.factions.FactionHandler
import org.jetbrains.exposed.sql.SizedIterable
import java.util.UUID

/**
 * Entry point for accessing basic Improved Factions functionality from other plugins.
 */
object ImprovedFactionsAPI {
    /**
     * Get all factions registered on the server.
     */
    fun getFactions(): SizedIterable<Faction> = FactionHandler.getFactions()

    /**
     * Get a faction by its id.
     */
    fun getFaction(id: Int): Faction? = FactionHandler.getFaction(id)

    /**
     * Get a faction by its name.
     */
    fun getFaction(name: String): Faction? = FactionHandler.getFaction(name)

    /**
     * Create a new faction.
     */
    fun createFaction(ownerId: UUID, factionName: String): Faction =
        FactionHandler.createFaction(ownerId, factionName)
}
