package com.wingedsheep.engine.handlers.cast

import com.wingedsheep.engine.registry.CardRegistry
import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.ZoneKey
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.model.EntityId
import com.wingedsheep.sdk.scripting.AdditionalCost
import com.wingedsheep.sdk.scripting.MayCastFromGraveyardWithAdditionalDiscardCost

/**
 * Grants a legal cast from graveyard for spells tagged with
 * [MayCastFromGraveyardWithAdditionalDiscardCost], attaches an implied discard cost,
 * and relies on normal (non-exile) post-resolution zone routing.
 */
class CastFromGraveyardWithAdditionalDiscardCostHandler(
    private val cardRegistry: CardRegistry
) {
    fun hasPermission(state: GameState, playerId: EntityId, cardId: EntityId): Boolean {
        if (cardId !in state.getZone(ZoneKey(playerId, Zone.GRAVEYARD))) return false
        val cardComponent = state.getEntity(cardId)?.get<CardComponent>() ?: return false
        val cardDef = cardRegistry.getCard(cardComponent.cardDefinitionId) ?: return false
        return cardDef.script.staticAbilities.any { it is MayCastFromGraveyardWithAdditionalDiscardCost }
    }

    fun getImpliedDiscardCost(state: GameState, cardId: EntityId): AdditionalCost.DiscardCards? {
        val cardComponent = state.getEntity(cardId)?.get<CardComponent>() ?: return null
        val cardDef = cardRegistry.getCard(cardComponent.cardDefinitionId) ?: return null
        val permission = cardDef.script.staticAbilities
            .filterIsInstance<MayCastFromGraveyardWithAdditionalDiscardCost>()
            .firstOrNull() ?: return null
        return AdditionalCost.DiscardCards(count = permission.discardCount)
    }
}
