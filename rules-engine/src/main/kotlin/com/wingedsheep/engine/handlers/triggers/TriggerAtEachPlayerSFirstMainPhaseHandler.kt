package com.wingedsheep.engine.handlers.triggers

import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.EntityId
import com.wingedsheep.sdk.scripting.GameEvent
import com.wingedsheep.sdk.scripting.TriggeredAbility
import com.wingedsheep.sdk.scripting.references.Player

/**
 * Resolves the ability controller for "at the beginning of each player's first main phase"
 * triggers (StepEvent(PRECOMBAT_MAIN, Player.Each)).
 *
 * MTG rule: when such a trigger says "that player", the controller of the triggered ability
 * is the player whose precombat main phase began — not the permanent's controller.
 */
object TriggerAtEachPlayerSFirstMainPhaseHandler {

    fun resolveController(
        ability: TriggeredAbility,
        permanentControllerId: EntityId,
        activePlayerId: EntityId
    ): EntityId {
        val trigger = ability.trigger
        return if (trigger is GameEvent.StepEvent &&
            trigger.step == Step.PRECOMBAT_MAIN &&
            trigger.player == Player.Each
        ) {
            activePlayerId
        } else {
            permanentControllerId
        }
    }
}
