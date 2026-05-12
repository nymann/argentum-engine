package com.wingedsheep.engine.handlers.alternativecosts

import com.wingedsheep.engine.core.CastSpell
import com.wingedsheep.engine.core.GameEvent
import com.wingedsheep.engine.core.ZoneChangeEvent
import com.wingedsheep.engine.handlers.PredicateContext
import com.wingedsheep.engine.handlers.PredicateEvaluator
import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.ZoneKey
import com.wingedsheep.engine.state.components.battlefield.TappedComponent
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.state.components.identity.ControllerComponent
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.model.EntityId
import com.wingedsheep.sdk.scripting.AdditionalCost

/**
 * Validates and executes the Web-slinging alternative-cost bounce payment:
 * the caster returns a tapped creature they control to its owner's hand.
 *
 * This handler is called from the [CastSpellHandler] additional-cost machinery
 * whenever a [AdditionalCost.ReturnTappedCreatureToHand] cost appears in a spell's
 * [com.wingedsheep.sdk.scripting.SelfAlternativeCost].
 */
class WebSlingingHandler {

    private val predicateEvaluator = PredicateEvaluator()

    /**
     * Validates that the player's [AdditionalCostPayment.bouncedPermanents] selection
     * satisfies the [AdditionalCost.ReturnTappedCreatureToHand] cost.
     *
     * @return an error message if invalid, null if the cost can be paid.
     */
    fun validateBounceCost(
        state: GameState,
        cost: AdditionalCost.ReturnTappedCreatureToHand,
        action: CastSpell
    ): String? {
        val projected = state.projectedState
        val bounced = action.additionalCostPayment?.bouncedPermanents ?: emptyList()

        if (bounced.size < cost.count) {
            return "Web-slinging requires returning ${cost.count} tapped " +
                "${cost.filter.description}(s) you control to its owner's hand"
        }

        val context = PredicateContext(controllerId = action.playerId)
        for (permId in bounced) {
            val container = state.getEntity(permId)
                ?: return "Permanent to return not found: $permId"
            val card = container.get<CardComponent>()
                ?: return "Entity to return is not a card: $permId"
            if (permId !in state.getBattlefield()) {
                return "Permanent to return is not on the battlefield: $permId"
            }
            if (projected.getController(permId) != action.playerId) {
                return "You can only return permanents you control via Web-slinging"
            }
            if (!container.has<TappedComponent>()) {
                return "${card.name} is not tapped — Web-slinging requires a tapped permanent"
            }
            if (!predicateEvaluator.matchesWithProjection(state, projected, permId, cost.filter, context)) {
                return "${card.name} doesn't match the required filter for the Web-slinging bounce cost"
            }
        }
        return null
    }

    /**
     * Executes the bounce: moves each permanent in [bouncedPermanents] from
     * the battlefield to its owner's hand and emits [ZoneChangeEvent]s.
     */
    fun executeBounce(
        state: GameState,
        bouncedPermanents: List<EntityId>
    ): Pair<GameState, List<GameEvent>> {
        var currentState = state
        val events = mutableListOf<GameEvent>()

        for (permId in bouncedPermanents) {
            val container = currentState.getEntity(permId) ?: continue
            val card = container.get<CardComponent>() ?: continue
            val controllerId = container.get<ControllerComponent>()?.playerId ?: continue
            val ownerId = card.ownerId ?: controllerId

            val battlefieldZone = ZoneKey(controllerId, Zone.BATTLEFIELD)
            val handZone = ZoneKey(ownerId, Zone.HAND)

            currentState = currentState.removeFromZone(battlefieldZone, permId)
            currentState = currentState.addToZone(handZone, permId)

            events.add(
                ZoneChangeEvent(
                    entityId = permId,
                    entityName = card.name,
                    fromZone = Zone.BATTLEFIELD,
                    toZone = Zone.HAND,
                    ownerId = ownerId
                )
            )
        }

        return Pair(currentState, events)
    }
}
