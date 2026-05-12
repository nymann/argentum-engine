package com.wingedsheep.engine.handlers.effects

import com.wingedsheep.engine.core.ConniveContinuation
import com.wingedsheep.engine.core.DecisionPhase
import com.wingedsheep.engine.core.EffectResult
import com.wingedsheep.engine.handlers.DecisionHandler
import com.wingedsheep.engine.handlers.EffectContext
import com.wingedsheep.engine.handlers.effects.drawing.DrawCardPrimitive
import com.wingedsheep.engine.handlers.effects.drawing.DrawLoop
import com.wingedsheep.engine.registry.CardRegistry
import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.ZoneKey
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.scripting.effects.ConniveEffect
import kotlin.reflect.KClass

class ConniveEffectHandler(
    private val cardRegistry: CardRegistry,
    private val decisionHandler: DecisionHandler = DecisionHandler()
) : EffectExecutor<ConniveEffect> {

    override val effectType: KClass<ConniveEffect> = ConniveEffect::class

    private val drawPrimitive by lazy { DrawCardPrimitive(cardRegistry) }

    override fun execute(
        state: GameState,
        effect: ConniveEffect,
        context: EffectContext
    ): EffectResult {
        val controllerId = context.controllerId
        val conniveSourceId = context.sourceId
            ?: return EffectResult.error(state, "Connive requires a source permanent")

        // Step 1: Draw 1 card
        val drawResult = DrawLoop.run(
            state = state,
            playerId = controllerId,
            count = 1,
            primitive = drawPrimitive,
            dispatcher = null,
            isDrawStep = false
        )

        if (drawResult.isPaused) return drawResult

        val stateAfterDraw = drawResult.state
        val drawEvents = drawResult.events

        // Step 2: Pause for the controller to choose a card to discard
        val handZone = ZoneKey(controllerId, Zone.HAND)
        val hand = stateAfterDraw.getZone(handZone)

        if (hand.isEmpty()) {
            return EffectResult.success(stateAfterDraw, drawEvents)
        }

        val sourceName = state.getEntity(conniveSourceId)?.get<CardComponent>()?.name

        val decisionResult = decisionHandler.createCardSelectionDecision(
            state = stateAfterDraw,
            playerId = controllerId,
            sourceId = conniveSourceId,
            sourceName = sourceName,
            prompt = "Choose a card to discard",
            options = hand,
            minSelections = 1,
            maxSelections = 1,
            ordered = false,
            phase = DecisionPhase.RESOLUTION
        )

        val continuation = ConniveContinuation(
            decisionId = decisionResult.pendingDecision!!.id,
            controllerId = controllerId,
            conniveSourceId = conniveSourceId
        )

        val stateWithContinuation = decisionResult.state.pushContinuation(continuation)

        return EffectResult.paused(
            stateWithContinuation,
            decisionResult.pendingDecision,
            drawEvents + decisionResult.events
        )
    }
}
