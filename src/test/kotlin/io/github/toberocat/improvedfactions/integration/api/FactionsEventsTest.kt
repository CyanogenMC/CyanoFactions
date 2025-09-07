package io.github.toberocat.improvedfactions.integration.api

import io.github.toberocat.improvedfactions.ImprovedFactionsTest
import io.github.toberocat.improvedfactions.api.events.FactionCreateEvent
import io.github.toberocat.improvedfactions.api.events.FactionDeleteEvent
import io.github.toberocat.improvedfactions.api.events.FactionJoinEvent
import io.github.toberocat.improvedfactions.api.events.FactionLeaveEvent
import io.github.toberocat.improvedfactions.factions.FactionHandler
import io.github.toberocat.improvedfactions.user.factionUser
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class FactionEventsTest : ImprovedFactionsTest() {

    @Test
    fun `test faction create event is fired`() {
        val eventFired = AtomicBoolean(false)
        val capturedFaction = AtomicReference<FactionCreateEvent>()

        val listener = object : Listener {
            @EventHandler
            fun onFactionCreate(event: FactionCreateEvent) {
                eventFired.set(true)
                capturedFaction.set(event)
            }
        }

        server.pluginManager.registerEvents(listener, plugin)

        val player = createTestPlayer("TestPlayer")
        val faction = FactionHandler.createFaction(player.uniqueId, "TestFaction")

        assertTrue(eventFired.get(), "FactionCreateEvent was not fired")
        assertEquals(faction.id, capturedFaction.get().faction.id, "Event contained incorrect faction")
    }

    @Test
    fun `test faction leave event is fired`() {
        val eventFired = AtomicBoolean(false)
        val capturedEvent = AtomicReference<FactionLeaveEvent>()

        val listener = object : Listener {
            @EventHandler
            fun onFactionLeave(event: FactionLeaveEvent) {
                eventFired.set(true)
                capturedEvent.set(event)
            }
        }

        server.pluginManager.registerEvents(listener, plugin)

        val owningPlayer = createTestPlayer("OwningPlayer")
        val player = createTestPlayer("LeavingPlayer")
        val faction = testFaction(owningPlayer.uniqueId)

        transaction {
            faction.join(player.uniqueId, 1)
            faction.leave(player.uniqueId)
        }

        assertTrue(eventFired.get(), "FactionLeaveEvent was not fired")
        assertEquals(faction.id, capturedEvent.get().faction.id, "Event contained incorrect faction")
        assertEquals(player.uniqueId, capturedEvent.get().user.uniqueId, "Event contained incorrect user")
    }

    @Test
    fun `test faction join event is fired`() {
        val eventFired = AtomicBoolean(false)
        val capturedEvent = AtomicReference<FactionJoinEvent>()

        val listener = object : Listener {
            @EventHandler
            fun onFactionLeave(event: FactionJoinEvent) {
                eventFired.set(true)
                capturedEvent.set(event)
            }
        }

        server.pluginManager.registerEvents(listener, plugin)

        val owningPlayer = createTestPlayer("OwningPlayer")
        val player = createTestPlayer("JoiningPlayer")
        val faction = testFaction(owningPlayer.uniqueId)

        transaction {
            faction.join(player.uniqueId, 1)
        }

        assertTrue(eventFired.get(), "FactionJoinEvent was not fired")
        assertEquals(faction.id, capturedEvent.get().faction.id, "Event contained incorrect faction")
        assertEquals(player.uniqueId, capturedEvent.get().user.uniqueId, "Event contained incorrect user")
    }

    @Test
    fun `test faction delete event is fired`() {
        val eventFired = AtomicBoolean(false)
        val membersPersisted = AtomicBoolean(false)
        val capturedEvent = AtomicReference<FactionDeleteEvent>()

        val listener = object : Listener {
            @EventHandler
            fun onFactionLeave(event: FactionDeleteEvent) {
                eventFired.set(true)
                capturedEvent.set(event)
                membersPersisted.set(event.faction.members().count() != 0L)
            }
        }

        server.pluginManager.registerEvents(listener, plugin)

        val owningPlayer = createTestPlayer("OwningPlayer")
        val faction = testFaction(owningPlayer.uniqueId)

        transaction {
            faction.delete()
        }

        assertTrue(eventFired.get(), "FactionDeleteEvent was not fired")
        assertEquals(faction.id, capturedEvent.get().faction.id, "Event contained incorrect faction")
        assertTrue(membersPersisted.get(), "Member persisted should not have been deleted")
    }

    @Test
    fun `test faction join event can be cancelled`() {
        val eventHandled = AtomicBoolean(false)
        val joinCancelled = AtomicBoolean(false)

        val listener = object : Listener {
            @EventHandler
            fun onFactionJoin(event: FactionJoinEvent) {
                eventHandled.set(true)
                event.setCancelled(true)
            }
        }

        server.pluginManager.registerEvents(listener, plugin)

        val owner = createTestPlayer("OwnerPlayer")
        val player = createTestPlayer("JoiningPlayer")
        val faction = testFaction(owner.uniqueId)

        transaction {
            faction.join(player.uniqueId, 1)
            // If join was cancelled, player should not be in faction
            joinCancelled.set(player.uniqueId.factionUser().factionId != faction.id.value)
        }

        assertTrue(eventHandled.get(), "FactionJoinEvent was not handled")
        assertTrue(joinCancelled.get(), "Join was not cancelled despite event being cancelled")
    }

    @Test
    fun `test faction leave event can be cancelled`() {
        val eventHandled = AtomicBoolean(false)
        val leaveCancelled = AtomicBoolean(false)

        val listener = object : Listener {
            @EventHandler
            fun onFactionLeave(event: FactionLeaveEvent) {
                eventHandled.set(true)
                event.setCancelled(true)
            }
        }

        server.pluginManager.registerEvents(listener, plugin)

        val owner = createTestPlayer("OwnerPlayer")
        val player = createTestPlayer("LeavingPlayer")
        val faction = testFaction(owner.uniqueId)

        transaction {
            faction.join(player.uniqueId, 1)

            // Try to leave
            faction.leave(player.uniqueId)

            // If leave was cancelled, player should still be in faction
            leaveCancelled.set(player.uniqueId.factionUser().factionId == faction.id.value)
        }

        assertTrue(eventHandled.get(), "FactionLeaveEvent was not handled")
        assertTrue(leaveCancelled.get(), "Leave was not cancelled despite event being cancelled")
    }

    @Test
    fun `test faction delete event can be cancelled`() {
        val eventHandled = AtomicBoolean(false)
        val deleteCancelled = AtomicBoolean(false)

        val listener = object : Listener {
            @EventHandler
            fun onFactionDelete(event: FactionDeleteEvent) {
                eventHandled.set(true)
                event.setCancelled(true)
            }
        }

        server.pluginManager.registerEvents(listener, plugin)

        val owner = createTestPlayer("OwnerPlayer")
        val faction = testFaction(owner.uniqueId)
        val factionId = faction.id.value

        transaction {
            faction.delete()

            // Try to fetch the faction - if delete was cancelled, it should still exist
            val stillExists = FactionHandler.getFaction(factionId) != null
            deleteCancelled.set(stillExists)
        }

        assertTrue(eventHandled.get(), "FactionDeleteEvent was not handled")
        assertTrue(deleteCancelled.get(), "Delete was not cancelled despite event being cancelled")
    }
}