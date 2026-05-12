package com.wingedsheep.engine.handlers.effects

import com.wingedsheep.engine.core.EffectResult
import com.wingedsheep.engine.handlers.EffectContext
import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.components.identity.MayPlayFromExileComponent
import com.wingedsheep.sdk.model.EntityId

/**
 * Effect that tags a set of specific exiled card entities with a play permission
 * owned by the effect controller, bounded by "until end of your next turn".
 *
 * The permission expires at the cleanup step of the controller's next turn.
 * [MayPlayFromExileComponent.expiresAfterTurn] is set to [GameState.turnNumber] + 1
 * because [GameState.turnNumber] increments once per round (when the first player in
 * turn order starts their turn), so the controller's next turn arrives one increment later.
 *
 * Cleanup is handled by [com.wingedsheep.engine.core.CleanupPhaseManager.cleanupEndOfTurn],
 * which removes [MayPlayFromExileComponent] whenever
 * `state.turnNumber >= component.expiresAfterTurn`.
 *
 * @param cardIds  The entity IDs of the exiled cards to tag.
 */
data class GrantUntilEndOfNextTurnExilePlayPermissionEffect(
    val cardIds: List<EntityId>
)

/**
 * Executor for [GrantUntilEndOfNextTurnExilePlayPermissionEffect].
 *
 * Attaches [MayPlayFromExileComponent] to each card in [GrantUntilEndOfNextTurnExilePlayPermissionEffect.cardIds].
 * The permission is scoped to the effect controller and expires at the cleanup of their next turn.
 */
class TemporaryPlayFromExilePermissionHandler {

    fun execute(
        state: GameState,
        effect: GrantUntilEndOfNextTurnExilePlayPermissionEffect,
        context: EffectContext
    ): EffectResult {
        val controllerId = context.controllerId
        val expiresAfterTurn = state.turnNumber + 1

        var newState = state
        for (cardId in effect.cardIds) {
            newState = newState.updateEntity(cardId) { container ->
                container.with(
                    MayPlayFromExileComponent(
                        controllerId     = controllerId,
                        expiresAfterTurn = expiresAfterTurn,
                        permanent        = false
                    )
                )
            }
        }

        return EffectResult.success(newState)
    }
}
