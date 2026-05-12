package com.wingedsheep.engine.handlers.effects

import com.wingedsheep.engine.core.EffectResult
import com.wingedsheep.engine.handlers.EffectContext
import com.wingedsheep.engine.handlers.effects.TargetResolutionUtils.toEntityId
import com.wingedsheep.engine.state.GameState
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.scripting.effects.ReturnSelfAndTargetLandCardFromGraveyardToBattlefieldTappedEffect
import kotlin.reflect.KClass

class ReturnSelfAndTargetLandCardFromGraveyardToBattlefieldTappedHandler :
    EffectExecutor<ReturnSelfAndTargetLandCardFromGraveyardToBattlefieldTappedEffect> {

    override val effectType: KClass<ReturnSelfAndTargetLandCardFromGraveyardToBattlefieldTappedEffect> =
        ReturnSelfAndTargetLandCardFromGraveyardToBattlefieldTappedEffect::class

    override fun execute(
        state: GameState,
        effect: ReturnSelfAndTargetLandCardFromGraveyardToBattlefieldTappedEffect,
        context: EffectContext
    ): EffectResult {
        val sourceId = context.sourceId
            ?: return EffectResult.error(state, "No source card")

        val targetId = context.targets.firstOrNull()
            ?.let { TargetResolutionUtils.run { it.toEntityId() } }
            ?: return EffectResult.error(state, "No target land card chosen")

        val entryOptions = ZoneEntryOptions(controllerId = context.controllerId, tapped = true)

        val selfResult = ZoneTransitionService.moveToZone(state, sourceId, Zone.BATTLEFIELD, entryOptions)
        val landResult = ZoneTransitionService.moveToZone(selfResult.state, targetId, Zone.BATTLEFIELD, entryOptions)

        return EffectResult.success(landResult.state, selfResult.events + landResult.events)
    }
}
