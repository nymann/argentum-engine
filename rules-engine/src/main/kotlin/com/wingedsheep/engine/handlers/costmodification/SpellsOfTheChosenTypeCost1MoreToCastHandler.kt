package com.wingedsheep.engine.handlers.costmodification

import com.wingedsheep.engine.registry.CardRegistry
import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.components.battlefield.ClassLevelComponent
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.state.components.identity.ChosenCardTypeComponent
import com.wingedsheep.sdk.model.CardDefinition
import com.wingedsheep.sdk.model.EntityId
import com.wingedsheep.sdk.scripting.SpellsOfChosenTypeCost1MoreToCast

/**
 * Computes the generic cost increase imposed by any [SpellsOfChosenTypeCost1MoreToCast]
 * permanent on the battlefield whose [ChosenCardTypeComponent] matches the card type of
 * the spell being cast.
 *
 * Each qualifying permanent contributes +{1} to the spell's cost independently. The
 * result is accumulated into CostCalculator's totalIncrease bucket so that normal
 * cost-reduction effects (ReduceGeneric, etc.) can still lower the final total.
 */
class SpellsOfTheChosenTypeCost1MoreToCastHandler(
    private val cardRegistry: CardRegistry,
) {
    fun computeIncrease(state: GameState, cardDef: CardDefinition, casterId: EntityId): Int {
        var increase = 0
        for (playerId in state.turnOrder) {
            for (entityId in state.getBattlefield(playerId)) {
                val container = state.getEntity(entityId) ?: continue
                val card = container.get<CardComponent>() ?: continue
                val permanentDef = cardRegistry.getCard(card.cardDefinitionId) ?: continue
                val classLevel = container.get<ClassLevelComponent>()?.currentLevel
                val hasAbility = permanentDef.script.effectiveStaticAbilities(classLevel)
                    .any { it is SpellsOfChosenTypeCost1MoreToCast }
                if (!hasAbility) continue
                val chosenType = container.get<ChosenCardTypeComponent>()?.chosenCardType ?: continue
                if (cardDef.typeLine.cardTypes.any { it.displayName.equals(chosenType, ignoreCase = true) }) {
                    increase += 1
                }
            }
        }
        return increase
    }
}
