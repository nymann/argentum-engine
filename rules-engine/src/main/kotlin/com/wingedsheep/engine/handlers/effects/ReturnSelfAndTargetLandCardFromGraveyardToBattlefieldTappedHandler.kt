package com.wingedsheep.engine.handlers.effects

import com.wingedsheep.engine.core.EffectResult
import com.wingedsheep.engine.handlers.EffectContext
import com.wingedsheep.engine.state.GameState
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
    ): EffectResult = EffectResult.error(state, "not implemented")
}
