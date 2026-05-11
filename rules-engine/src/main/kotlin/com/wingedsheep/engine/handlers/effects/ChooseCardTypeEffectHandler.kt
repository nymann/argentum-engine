package com.wingedsheep.engine.handlers.effects

import com.wingedsheep.engine.core.*
import com.wingedsheep.engine.handlers.EffectContext
import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.sdk.core.CardType
import com.wingedsheep.sdk.scripting.effects.ChooseCardTypeEffect
import java.util.UUID
import kotlin.reflect.KClass

class ChooseCardTypeEffectHandler : EffectExecutor<ChooseCardTypeEffect> {

    override val effectType: KClass<ChooseCardTypeEffect> = ChooseCardTypeEffect::class

    override fun execute(
        state: GameState,
        effect: ChooseCardTypeEffect,
        context: EffectContext
    ): EffectResult {
        val controllerId = context.controllerId
        val sourceName = context.sourceId?.let { state.getEntity(it)?.get<CardComponent>()?.name }

        val excludedLower = effect.excludedCardTypes.map { it.lowercase() }.toSet()
        val options = CardType.entries
            .map { it.displayName }
            .filter { it.lowercase() !in excludedLower }

        val prompt = effect.prompt ?: buildString {
            append("Choose a card type")
            if (effect.excludedCardTypes.isNotEmpty()) {
                append(" other than ${effect.excludedCardTypes.joinToString(" or ")}")
            }
        }

        val decisionId = UUID.randomUUID().toString()
        val decision = ChooseOptionDecision(
            id = decisionId,
            playerId = controllerId,
            prompt = prompt,
            context = DecisionContext(
                sourceId = context.sourceId,
                sourceName = sourceName,
                phase = DecisionPhase.RESOLUTION
            ),
            options = options
        )

        val continuation = ChooseCardTypeContinuation(
            decisionId = decisionId,
            controllerId = controllerId,
            sourceId = context.sourceId,
            sourceName = sourceName,
            options = options
        )

        val stateWithDecision = state.withPendingDecision(decision)
        val stateWithContinuation = stateWithDecision.pushContinuation(continuation)

        return EffectResult.paused(
            stateWithContinuation,
            decision,
            listOf(
                DecisionRequestedEvent(
                    decisionId = decisionId,
                    playerId = controllerId,
                    decisionType = "CHOOSE_CARD_TYPE",
                    prompt = decision.prompt
                )
            )
        )
    }
}
