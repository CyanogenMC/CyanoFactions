package io.github.toberocat.improvedfactions.modules.relations.impl

import io.github.toberocat.improvedfactions.database.DatabaseManager.loggedTransaction
import io.github.toberocat.improvedfactions.factions.FactionHandler
import io.github.toberocat.improvedfactions.modules.relations.RelationType
import io.github.toberocat.improvedfactions.modules.relations.database.FactionAllyInvite
import io.github.toberocat.improvedfactions.modules.relations.database.FactionAllyInvites
import io.github.toberocat.improvedfactions.modules.relations.database.FactionRelation
import io.github.toberocat.improvedfactions.modules.relations.database.FactionRelations
import io.github.toberocat.improvedfactions.modules.relations.handles.RelationsModuleHandle
import io.github.toberocat.improvedfactions.translation.LocalizedException
import java.time.Duration
import kotlinx.datetime.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or

class RelationsModuleHandleImpl : RelationsModuleHandle {
    override fun getAlliedFactions(factionId: Int) =
            FactionRelation.find {
                        FactionRelations.relationType eq
                                RelationType.ALLY and
                                (FactionRelations.sourceFaction eq
                                        factionId or
                                        (FactionRelations.targetFaction eq factionId))
                    }
                    .map {
                        if (it.sourceFaction.id.value == factionId) it.targetFaction.id.value
                        else it.sourceFaction.id.value
                    }
                    .toSet()

    override fun getEnemyFactions(factionId: Int) =
            FactionRelation.find {
                        FactionRelations.relationType eq
                                RelationType.ENEMY and
                                (FactionRelations.sourceFaction eq
                                        factionId or
                                        (FactionRelations.targetFaction eq factionId))
                    }
                    .map {
                        if (it.sourceFaction.id.value == factionId) it.targetFaction.id.value
                        else it.sourceFaction.id.value
                    }
                    .toSet()

    override fun isAllied(factionId: Int, targetFactionId: Int) =
            FactionRelation.count(
                    FactionRelations.relationType eq
                            RelationType.ALLY and
                            ((FactionRelations.sourceFaction eq
                                    factionId and
                                    (FactionRelations.targetFaction eq targetFactionId)) or
                                    (FactionRelations.sourceFaction eq
                                            targetFactionId and
                                            (FactionRelations.targetFaction eq factionId)))
            ) > 0L

    override fun isEnemy(factionId: Int, targetFactionId: Int) =
            FactionRelation.count(
                    FactionRelations.relationType eq
                            RelationType.ENEMY and
                            ((FactionRelations.sourceFaction eq
                                    factionId and
                                    (FactionRelations.targetFaction eq targetFactionId)) or
                                    (FactionRelations.sourceFaction eq
                                            targetFactionId and
                                            (FactionRelations.targetFaction eq factionId)))
            ) > 0L

    override fun inviteToAlliance(factionId: Int, targetFactionId: Int) {
        if (factionId == targetFactionId)
                throw LocalizedException("relations.exceptions.cant-ally-yourself")
        if (isAllied(factionId, targetFactionId))
                throw LocalizedException("relations.exceptions.already-allied")
        if (isEnemy(factionId, targetFactionId))
                throw LocalizedException("relations.exceptions.already-enemy")
        if (FactionAllyInvite.find {
                            (FactionAllyInvites.sourceFaction eq
                                    factionId and
                                    (FactionAllyInvites.targetFaction eq targetFactionId)) or
                                    (FactionAllyInvites.sourceFaction eq
                                            targetFactionId and
                                            (FactionAllyInvites.targetFaction eq factionId))
                        }
                        .count() > 0
        )
                throw LocalizedException("relations.exceptions.already-invited")

        val faction = FactionHandler.getFaction(factionId) ?: return
        val targettedFaction = FactionHandler.getFaction(targetFactionId) ?: return

        targettedFaction.broadcast(
                "relations.faction.broadcasts.you-have-been-invited-to-ally",
                mapOf("faction" to faction.name)
        )
        faction.broadcast(
                "relations.faction.broadcasts.you-have-invited-to-ally",
                mapOf("faction" to targettedFaction.name)
        )
        FactionAllyInvite.new {
            sourceFaction = faction
            targetFaction = targettedFaction
            expirationDate =
                    Clock.System.now().plus(5, DateTimeUnit.MINUTE).toLocalDateTime(TimeZone.UTC)
        }
    }

    override fun acceptAlliance(factionId: Int, targetFactionId: Int) {
        if (factionId == targetFactionId)
                throw LocalizedException("relations.exceptions.cant-ally-yourself")
        if (isAllied(factionId, targetFactionId))
                throw LocalizedException("relations.exceptions.already-allied")
        if (isEnemy(factionId, targetFactionId))
                throw LocalizedException("relations.exceptions.already-enemy")

        val faction = FactionHandler.getFaction(factionId) ?: return
        val targettedFaction = FactionHandler.getFaction(targetFactionId) ?: return
        if (!FactionAllyInvite.find {
                            (FactionAllyInvites.sourceFaction eq
                                    targetFactionId and
                                    (FactionAllyInvites.targetFaction eq factionId)) or
                                    (FactionAllyInvites.sourceFaction eq
                                            factionId and
                                            (FactionAllyInvites.targetFaction eq targetFactionId))
                        }
                        .any()
        )
                throw LocalizedException("relations.exceptions.no-invite")

        targettedFaction.broadcast(
                "relations.faction.broadcasts.you-have-accepted-ally",
                mapOf("faction" to faction.name)
        )
        faction.broadcast(
                "relations.faction.broadcasts.you-have-accepted-ally",
                mapOf("faction" to targettedFaction.name)
        )
        val invite =
                FactionAllyInvite.find {
                            (FactionAllyInvites.sourceFaction eq
                                    targetFactionId and
                                    (FactionAllyInvites.targetFaction eq factionId)) or
                                    (FactionAllyInvites.sourceFaction eq
                                            factionId and
                                            (FactionAllyInvites.targetFaction eq targetFactionId))
                        }
                        .firstOrNull()
                        ?: return
        if (invite.expirationDate < Clock.System.now().toLocalDateTime(TimeZone.UTC)) {
            invite.delete()
            throw LocalizedException("relations.exceptions.invite-expired")
        }

        invite.delete()
        FactionRelation.new {
            sourceFaction = faction
            targetFaction = targettedFaction
            relationType = RelationType.ALLY
        }
    }

    override fun declareWar(factionId: Int, targetFactionId: Int) {
        if (factionId == targetFactionId)
                throw LocalizedException("relations.exceptions.cant-declare-war-on-yourself")
        if (isAllied(factionId, targetFactionId))
                throw LocalizedException("relations.exceptions.already-allied")
        if (isEnemy(factionId, targetFactionId))
                throw LocalizedException("relations.exceptions.already-enemy")

        val faction = FactionHandler.getFaction(factionId) ?: return
        val targettedFaction = FactionHandler.getFaction(targetFactionId) ?: return
        if (!targettedFaction.members().any { it.offlinePlayer().isOnline } &&
                        !targettedFaction.isInactive(Duration.ofDays(7).toMillis())
        )
                throw LocalizedException("relations.exceptions.target-faction-not-online")

        targettedFaction.broadcast(
                "relations.faction.broadcasts.you-have-been-declared-war",
                mapOf("faction" to faction.name)
        )
        faction.broadcast(
                "relations.faction.broadcasts.you-have-declared-war",
                mapOf("faction" to targettedFaction.name)
        )
        FactionRelation.new {
            sourceFaction = faction
            targetFaction = targettedFaction
            relationType = RelationType.ENEMY
        }
    }

    override fun breakAlliance(factionId: Int, targetFactionId: Int) {
        if (factionId == targetFactionId)
                throw LocalizedException("relations.exceptions.cant-break-alliance-with-yourself")
        if (!isAllied(factionId, targetFactionId))
                throw LocalizedException("relations.exceptions.not-allied")

        val faction = FactionHandler.getFaction(factionId) ?: return
        val targettedFaction = FactionHandler.getFaction(targetFactionId) ?: return

        targettedFaction.broadcast(
                "relations.faction.broadcasts.you-have-been-broken-alliance",
                mapOf("faction" to faction.name)
        )
        faction.broadcast(
                "relations.faction.broadcasts.you-have-broken-alliance",
                mapOf("faction" to targettedFaction.name)
        )
        FactionRelation.find {
            (FactionRelations.sourceFaction eq
                    factionId and
                    (FactionRelations.targetFaction eq targetFactionId)) or
                    (FactionRelations.sourceFaction eq
                            targetFactionId and
                            (FactionRelations.targetFaction eq factionId))
        }
                .forEach { it.delete() }
    }

    // ToDo: Make Peace will probably need to be reworked, as this has no consequences for the
    // factions.
    override fun makePeace(factionId: Int, targetFactionId: Int) {
        if (factionId == targetFactionId)
                throw LocalizedException("relations.exceptions.cant-make-peace-with-yourself")
        if (!isEnemy(factionId, targetFactionId))
                throw LocalizedException("relations.exceptions.not-enemy")

        val faction = FactionHandler.getFaction(factionId) ?: return
        val targettedFaction = FactionHandler.getFaction(targetFactionId) ?: return

        targettedFaction.broadcast(
                "relations.faction.broadcasts.you-have-been-made-peace",
                mapOf("faction" to faction.name)
        )
        faction.broadcast(
                "relations.faction.broadcasts.you-have-made-peace",
                mapOf("faction" to targettedFaction.name)
        )
        FactionRelation.find {
            (FactionRelations.sourceFaction eq
                    factionId and
                    (FactionRelations.targetFaction eq targetFactionId)) or
                    (FactionRelations.sourceFaction eq
                            targetFactionId and
                            (FactionRelations.targetFaction eq factionId))
        }
                .forEach { it.delete() }
    }

    override fun deleteFactionRelations(factionId: Int) = loggedTransaction {
        FactionAllyInvite.find {
            FactionAllyInvites.sourceFaction eq
                    factionId or
                    (FactionAllyInvites.targetFaction eq factionId)
        }
                .forEach { it.delete() }
        FactionRelation.find {
            FactionRelations.sourceFaction eq
                    factionId or
                    (FactionRelations.targetFaction eq factionId)
        }
                .forEach { it.delete() }
    }
}
