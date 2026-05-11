package com.wingedsheep.engine.handlers.effects.replacement

import com.wingedsheep.engine.core.CountersAddedEvent
import com.wingedsheep.engine.core.EffectResult
import com.wingedsheep.engine.core.GameEvent as EngineGameEvent
import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.components.battlefield.CountersComponent
import com.wingedsheep.engine.state.components.battlefield.ReplacementEffectSourceComponent
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.sdk.core.CounterType
import com.wingedsheep.sdk.model.EntityId
import com.wingedsheep.sdk.scripting.PreventDamage
import com.wingedsheep.sdk.scripting.events.RecipientFilter

object PreventDamageDealtToSelfAndPutThatMany11CountersOnItHandler {

    fun apply(
        state: GameState,
        targetId: EntityId,
        amount: Int
    ): EffectResult? {
        if (amount <= 0) return null

        val container = state.getEntity(targetId) ?: return null
        val replacementComponent = container.get<ReplacementEffectSourceComponent>() ?: return null

        val hasEffect = replacementComponent.replacementEffects.any { effect ->
            if (effect !is PreventDamage) return@any false
            val damageEvent = effect.appliesTo
            if (damageEvent !is com.wingedsheep.sdk.scripting.GameEvent.DamageEvent) return@any false
            damageEvent.recipient is RecipientFilter.Self
        }

        if (!hasEffect) return null

        val events = mutableListOf<EngineGameEvent>()
        var newState = state

        val currentCounters = container.get<CountersComponent>() ?: CountersComponent()
        newState = newState.updateEntity(targetId) { c ->
            c.with(currentCounters.withAdded(CounterType.PLUS_ONE_PLUS_ONE, amount))
        }

        val entityName = container.get<CardComponent>()?.name ?: ""
        events.add(CountersAddedEvent(targetId, CounterType.PLUS_ONE_PLUS_ONE.name, amount, entityName))

        return EffectResult.success(newState, events)
    }
}
