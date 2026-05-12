package com.wingedsheep.engine.handlers.effects

import com.wingedsheep.engine.handlers.EffectContext
import com.wingedsheep.engine.state.ComponentContainer
import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.ZoneKey
import com.wingedsheep.engine.state.components.battlefield.TappedComponent
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.state.components.identity.ControllerComponent
import com.wingedsheep.engine.state.components.identity.OwnerComponent
import com.wingedsheep.engine.state.components.stack.ChosenTarget
import com.wingedsheep.sdk.core.CardType
import com.wingedsheep.sdk.core.ManaCost
import com.wingedsheep.sdk.core.TypeLine
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.model.EntityId
import com.wingedsheep.sdk.scripting.effects.ReturnSelfAndTargetLandCardFromGraveyardToBattlefieldTappedEffect
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe

class ReturnSelfAndTargetLandCardFromGraveyardToBattlefieldTappedTest : FunSpec({

    val handler = ReturnSelfAndTargetLandCardFromGraveyardToBattlefieldTappedHandler()

    val playerId = EntityId.generate()
    val sourceCreatureId = EntityId.generate()
    val landCardId = EntityId.generate()
    val nonLandCardId = EntityId.generate()

    fun creatureCard(ownerId: EntityId) = CardComponent(
        cardDefinitionId = "Sandman",
        name = "Sandman",
        manaCost = ManaCost(emptyList()),
        typeLine = TypeLine(cardTypes = setOf(CardType.CREATURE)),
        ownerId = ownerId
    )

    fun landCard(ownerId: EntityId) = CardComponent(
        cardDefinitionId = "Forest",
        name = "Forest",
        manaCost = ManaCost(emptyList()),
        typeLine = TypeLine(cardTypes = setOf(CardType.LAND)),
        ownerId = ownerId
    )

    fun nonLandCard(ownerId: EntityId) = CardComponent(
        cardDefinitionId = "Giant Growth",
        name = "Giant Growth",
        manaCost = ManaCost(emptyList()),
        typeLine = TypeLine(cardTypes = setOf(CardType.INSTANT)),
        ownerId = ownerId
    )

    fun buildState(): GameState {
        val graveyardZone = ZoneKey(playerId, Zone.GRAVEYARD)
        var state = GameState().withEntity(playerId, ComponentContainer())

        state = state
            .withEntity(sourceCreatureId, ComponentContainer()
                .with(creatureCard(playerId))
                .with(OwnerComponent(playerId)))
            .addToZone(graveyardZone, sourceCreatureId)

        state = state
            .withEntity(landCardId, ComponentContainer()
                .with(landCard(playerId))
                .with(OwnerComponent(playerId)))
            .addToZone(graveyardZone, landCardId)

        state = state
            .withEntity(nonLandCardId, ComponentContainer()
                .with(nonLandCard(playerId))
                .with(OwnerComponent(playerId)))
            .addToZone(graveyardZone, nonLandCardId)

        return state
    }

    test("resolving the effect moves source card and selected land from graveyard to battlefield tapped; non-land stays in graveyard") {
        // GIVEN the active player's graveyard holds the source creature, one land card, and one non-land card
        val state = buildState()
        val effect = ReturnSelfAndTargetLandCardFromGraveyardToBattlefieldTappedEffect

        // WHEN the effect resolves with the player selecting the land card as the target
        val ctx = EffectContext(
            sourceId = sourceCreatureId,
            controllerId = playerId,
            opponentId = null,
            targets = listOf(ChosenTarget.Permanent(landCardId))
        )
        val result = handler.execute(state, effect, ctx)

        // THEN the effect resolves without error
        result.isSuccess shouldBe true

        val graveyardZone = ZoneKey(playerId, Zone.GRAVEYARD)
        val battlefieldZone = ZoneKey(playerId, Zone.BATTLEFIELD)

        // AND both the source creature and the selected land now exist on the battlefield
        result.state.getZone(battlefieldZone) shouldContain sourceCreatureId
        result.state.getZone(battlefieldZone) shouldContain landCardId

        // AND both battlefield permanents enter tapped
        result.state.getEntity(sourceCreatureId)!!.get<TappedComponent>() shouldBe TappedComponent
        result.state.getEntity(landCardId)!!.get<TappedComponent>() shouldBe TappedComponent

        // AND both cards left the graveyard
        result.state.getZone(graveyardZone) shouldNotContain sourceCreatureId
        result.state.getZone(graveyardZone) shouldNotContain landCardId

        // AND the non-land card was never moved — it remains in the graveyard
        result.state.getZone(graveyardZone) shouldContain nonLandCardId

        // AND the permanents are under the controller's control
        result.state.getEntity(sourceCreatureId)!!.get<ControllerComponent>()!!.playerId shouldBe playerId
        result.state.getEntity(landCardId)!!.get<ControllerComponent>()!!.playerId shouldBe playerId
    }
})
