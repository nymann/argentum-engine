package com.wingedsheep.engine.handlers.triggers

import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.components.battlefield.AttachedToComponent
import com.wingedsheep.engine.state.components.battlefield.CountersComponent
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.state.components.identity.ControllerComponent
import com.wingedsheep.sdk.core.Subtype
import com.wingedsheep.sdk.model.EntityId

/**
 * Evaluates the Rule 700.4 "modified" predicate used by
 * "whenever a modified creature you control deals combat damage to a player" triggers.
 *
 * A permanent is modified if it:
 * - has one or more counters on it, OR
 * - has one or more Equipment attached, OR
 * - is enchanted by one or more Auras its controller controls.
 */
object WheneverAModifiedCreatureYouControlDealsCombatDamageToAPlayerHandler {

    fun isModified(state: GameState, entityId: EntityId): Boolean {
        val entity = state.getEntity(entityId) ?: return false
        val controllerId = entity.get<ControllerComponent>()?.playerId ?: return false

        // Has any counter
        val counters = entity.get<CountersComponent>()
        if (counters != null && counters.counters.values.any { it > 0 }) return true

        // Has Equipment or Auras controlled by this permanent's controller attached
        for (permanentId in state.getBattlefield()) {
            if (permanentId == entityId) continue
            val container = state.getEntity(permanentId) ?: continue
            val attachedTo = container.get<AttachedToComponent>()?.targetId ?: continue
            if (attachedTo != entityId) continue
            val card = container.get<CardComponent>() ?: continue
            if (card.typeLine.hasSubtype(Subtype.EQUIPMENT)) return true
            if (card.typeLine.hasSubtype(Subtype.AURA)) {
                val auraController = container.get<ControllerComponent>()?.playerId
                if (auraController == controllerId) return true
            }
        }
        return false
    }
}
