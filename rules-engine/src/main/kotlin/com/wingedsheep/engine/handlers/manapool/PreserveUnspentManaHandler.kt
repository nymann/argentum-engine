package com.wingedsheep.engine.handlers.manapool

import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.components.player.ManaPoolComponent
import com.wingedsheep.engine.state.components.player.PreserveUnspentManaOfColorComponent

/**
 * Applies Rule 500.4 (empty mana pools at end of each step/phase) while
 * honouring any per-player [PreserveUnspentManaOfColorComponent] modifiers.
 */
class PreserveUnspentManaHandler {

    fun emptyManaPools(state: GameState): GameState {
        var newState = state
        for (playerId in newState.turnOrder) {
            newState = newState.updateEntity(playerId) { container ->
                val pool = container.get<ManaPoolComponent>() ?: return@updateEntity container
                if (pool.isEmpty) return@updateEntity container
                val preserve = container.get<PreserveUnspentManaOfColorComponent>()
                val newPool = if (preserve != null) {
                    pool.empty().add(preserve.color, pool.getAmount(preserve.color))
                } else {
                    pool.empty()
                }
                container.with(newPool)
            }
        }
        return newState
    }
}
