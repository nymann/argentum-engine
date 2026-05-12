package com.wingedsheep.engine.handlers.blocking

import com.wingedsheep.engine.state.Component
import com.wingedsheep.engine.state.ComponentContainer

/** Marks an attacker as restricted to at most one blocker (CR 509.1b). */
data object CanTBeBlockedByMoreThanOneCreature : Component

/**
 * Predicate + restriction for the cant-be-blocked-by-more-than-one primitive.
 * Predicate: the attacker bears [CanTBeBlockedByMoreThanOneCreature].
 * Restriction: the per-attacker blocker count must not exceed 1.
 */
object CanTBeBlockedByMoreThanOneCreatureRestriction {

    fun check(container: ComponentContainer, attackerName: String, blockerCount: Int): String? {
        if (!container.has<CanTBeBlockedByMoreThanOneCreature>()) return null
        if (blockerCount <= 1) return null
        return "$attackerName can't be blocked by more than one creature"
    }
}
