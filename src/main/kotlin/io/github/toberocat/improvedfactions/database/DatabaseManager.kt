package io.github.toberocat.improvedfactions.database

import io.github.toberocat.improvedfactions.claims.FactionClaims
import io.github.toberocat.improvedfactions.claims.clustering.cluster.Clusters
import io.github.toberocat.improvedfactions.claims.clustering.cluster.FactionClusters
import io.github.toberocat.improvedfactions.claims.clustering.cluster.ZoneClusters
import io.github.toberocat.improvedfactions.factions.Factions
import io.github.toberocat.improvedfactions.factions.ban.FactionBans
import io.github.toberocat.improvedfactions.invites.FactionInvites
import io.github.toberocat.improvedfactions.permissions.FactionPermissions
import io.github.toberocat.improvedfactions.ranks.FactionRankHandler
import io.github.toberocat.improvedfactions.ranks.FactionRanks
import io.github.toberocat.improvedfactions.user.FactionUsers
import io.github.toberocat.improvedfactions.utils.offline.KnownOfflinePlayers
import io.github.toberocat.improvedfactions.utils.options.limit.PlayerUsageLimits
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseManager {

    var verboseLogging: Boolean = false

    inline fun <T> loggedTransaction(crossinline statement: Transaction.() -> T): T = transaction {
        if (verboseLogging) {
            addLogger(StdOutSqlLogger)
        }

        statement()
    }

    fun initializeDatabase() {
        loggedTransaction {
            createTables(
                    FactionUsers,
                    FactionClaims,
                    FactionPermissions,
                    FactionBans,
                    PlayerUsageLimits,
                    Factions,
                    FactionRanks,
                    FactionInvites,
                    KnownOfflinePlayers,
                    Clusters,
                    FactionClusters,
                    ZoneClusters
            )

            runMigrations()

            Factions.handleQueues()
            FactionRankHandler.initRanks()
            FactionInvites.scheduleInviteExpirations()
        }
    }

    fun createTables(vararg tables: Table) = SchemaUtils.create(*tables)

    private fun Transaction.runMigrations() {
        try {
            val addColumnsStatements =
                    SchemaUtils.addMissingColumnsStatements(
                            FactionUsers,
                            FactionClaims,
                            FactionPermissions,
                            FactionBans,
                            PlayerUsageLimits,
                            Factions,
                            FactionRanks,
                            FactionInvites,
                            KnownOfflinePlayers,
                            Clusters,
                            FactionClusters,
                            ZoneClusters
                    )

            addColumnsStatements.forEach { statement ->
                try {
                    exec(statement)
                } catch (e: Exception) {
                    println("Migration statement failed (this may be expected): $statement")
                    println("Error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("Error during migration: ${e.message}")
        }
    }
}
