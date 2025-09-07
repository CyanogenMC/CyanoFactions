package io.github.toberocat.improvedfactions

import io.github.toberocat.improvedfactions.factions.Faction
import io.github.toberocat.improvedfactions.factions.FactionHandler
import io.github.toberocat.improvedfactions.modules.base.BaseModule
import org.bukkit.Material
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import java.util.UUID

open class ImprovedFactionsTest {
    protected lateinit var server: ServerMock
    protected lateinit var plugin: ImprovedFactionsPlugin

    @BeforeEach
    open fun setUp() {
        System.setProperty("bstats.relocatecheck", "false")
        server = MockBukkit.mock()
        plugin = MockBukkit.load(ImprovedFactionsPlugin::class.java)
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    fun createTestPlayer(name: String? = null) = server.addPlayer().also {
        it.isOp = true

        if (name != null) {
            it.name = name
        }
    }

    fun testWorld(name: String? = null) = WorldMock(Material.DIRT, 3).also {
        if (name != null) it.name = name
        BaseModule.config.allowedWorlds += it.name
    }

    fun testFaction(owner: UUID = UUID.randomUUID(), id: Int? = null, vararg members: UUID): Faction {
        var faction = transaction { id?.let { FactionHandler.getFaction(it) } }
        if (faction != null) {
            return faction
        }
        faction = FactionHandler.createFaction(owner, "TestFaction", id)
        transaction { members.forEach { faction.join(it, faction.defaultRank) } }
        return faction
    }

}