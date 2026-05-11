package com.wingedsheep.engine.handlers.effects.stack

import com.wingedsheep.engine.handlers.EffectContext
import com.wingedsheep.engine.registry.CardRegistry
import com.wingedsheep.engine.state.ComponentContainer
import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.ZoneKey
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.state.components.stack.SpellOnStackComponent
import com.wingedsheep.sdk.core.CardType
import com.wingedsheep.sdk.core.ManaCost
import com.wingedsheep.sdk.core.TypeLine
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.model.EntityId
import com.wingedsheep.sdk.scripting.effects.WardCost
import com.wingedsheep.sdk.scripting.effects.WardCounterEffect
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

/**
 * Direct rules-engine coverage for [WardCounterEffectExecutor.handleDiscardCost].
 *
 * The discard branch was introduced for the "Alpharael, Stonechosen" card and was only
 * exercised indirectly through a game-server scenario test. These tests pin down the
 * branch behaviour at the executor level so regressions in the discard logic surface
 * without depending on the higher-level scenario pipeline.
 */
class WardCounterEffectExecutorDiscardTest : FunSpec({

    val cardRegistry = CardRegistry()
    val executor = WardCounterEffectExecutor(cardRegistry)

    fun handCard(ownerId: EntityId, name: String): ComponentContainer =
        ComponentContainer.of(
            CardComponent(
                cardDefinitionId = name,
                name = name,
                manaCost = ManaCost(emptyList()),
                typeLine = TypeLine(cardTypes = setOf(CardType.SORCERY)),
                ownerId = ownerId,
            )
        )

    /**
     * Build a state with:
     *  - `casterId` player whose hand holds [handCards]
     *  - one spell on the stack (id = `spellId`) owned and cast by `casterId`,
     *    located in its caster's STACK zone so removal/counter routes work.
     */
    fun stateWithHandAndSpell(
        casterId: EntityId,
        spellId: EntityId,
        handCards: List<EntityId>,
    ): GameState {
        val spellCard = CardComponent(
            cardDefinitionId = "Test Spell",
            name = "Test Spell",
            manaCost = ManaCost(emptyList()),
            typeLine = TypeLine(cardTypes = setOf(CardType.INSTANT)),
            ownerId = casterId,
        )
        val spellContainer = ComponentContainer.of(
            spellCard,
            SpellOnStackComponent(casterId = casterId),
        )

        var state = GameState()
            .withEntity(casterId, ComponentContainer.EMPTY)
            .withEntity(spellId, spellContainer)
            .pushToStack(spellId)
            .addToZone(ZoneKey(casterId, Zone.STACK), spellId)

        for (cardId in handCards) {
            state = state
                .withEntity(cardId, handCard(casterId, "Hand-${cardId.value.take(4)}"))
                .addToZone(ZoneKey(casterId, Zone.HAND), cardId)
        }
        return state
    }

    fun context(spellId: EntityId, controllerId: EntityId) = EffectContext(
        sourceId = null,
        controllerId = controllerId,
        opponentId = null,
        targetingSourceEntityId = spellId,
    )

    test("hand smaller than discard cost — spell is countered, no cards discarded") {
        val caster = EntityId.generate()
        val warder = EntityId.generate()
        val spell = EntityId.generate()
        val hand = listOf(EntityId.generate(), EntityId.generate())
        val state = stateWithHandAndSpell(caster, spell, hand)
        val effect = WardCounterEffect(WardCost.Discard(count = 3, random = true))

        val result = executor.execute(state, effect, context(spell, warder))

        result.isSuccess shouldBe true
        // Spell removed from the stack and routed to its owner's graveyard.
        result.state.stack shouldBe emptyList()
        result.state.getZone(caster, Zone.GRAVEYARD) shouldContainExactly listOf(spell)
        // No cards were discarded: hand untouched, caster's graveyard contains only the
        // countered spell (no hand cards).
        result.state.getHand(caster) shouldContainExactly hand
    }

    test("hand equal to discard cost — entire hand discarded, spell remains on the stack") {
        val caster = EntityId.generate()
        val warder = EntityId.generate()
        val spell = EntityId.generate()
        val hand = listOf(EntityId.generate(), EntityId.generate())
        val state = stateWithHandAndSpell(caster, spell, hand)
        val effect = WardCounterEffect(WardCost.Discard(count = 2, random = true))

        val result = executor.execute(state, effect, context(spell, warder))

        result.isSuccess shouldBe true
        // Spell was not countered — still on the stack, still owned by caster.
        result.state.stack shouldContainExactly listOf(spell)
        // All two cards moved from hand to graveyard.
        result.state.getHand(caster) shouldBe emptyList()
        result.state.getZone(caster, Zone.GRAVEYARD) shouldContainExactlyInAnyOrder hand
    }

    test("hand larger than discard cost, deterministic — discards the front of the hand") {
        val caster = EntityId.generate()
        val warder = EntityId.generate()
        val spell = EntityId.generate()
        val first = EntityId.generate()
        val second = EntityId.generate()
        val third = EntityId.generate()
        val hand = listOf(first, second, third)
        val state = stateWithHandAndSpell(caster, spell, hand)
        val effect = WardCounterEffect(WardCost.Discard(count = 2, random = false))

        val result = executor.execute(state, effect, context(spell, warder))

        result.isSuccess shouldBe true
        // Spell stays on the stack — cost was paid.
        result.state.stack shouldContainExactly listOf(spell)
        // Deterministic path: `hand.take(count)` removes the first two entries; the third
        // remains in hand.
        result.state.getHand(caster) shouldContainExactly listOf(third)
        result.state.getZone(caster, Zone.GRAVEYARD) shouldContainExactly listOf(first, second)
    }

    test("hand larger than discard cost, random — exactly cost.count cards move from hand to graveyard") {
        val caster = EntityId.generate()
        val warder = EntityId.generate()
        val spell = EntityId.generate()
        val hand = (1..4).map { EntityId.generate() }
        val state = stateWithHandAndSpell(caster, spell, hand)
        val effect = WardCounterEffect(WardCost.Discard(count = 2, random = true))

        val result = executor.execute(state, effect, context(spell, warder))

        result.isSuccess shouldBe true
        // Spell still on the stack.
        result.state.stack shouldContainExactly listOf(spell)
        // Don't assert which cards: shuffle is unseeded. Assert counts and a partition.
        val newHand = result.state.getHand(caster)
        val newGraveyard = result.state.getZone(caster, Zone.GRAVEYARD)
        newHand.size shouldBe (hand.size - 2)
        newGraveyard.size shouldBe 2
        // Every original card is now either in hand or in graveyard — none lost.
        (newHand + newGraveyard) shouldContainExactlyInAnyOrder hand
    }

    test("discard cost of zero — spell resolves with no discard") {
        // WardCost.Discard(count = 0) is type-constructible (count is an unconstrained Int
        // with default = 1). The `hand.size < cost.count` guard is false (2 < 0), and
        // `hand.take(0)` yields an empty list, so the loop is a no-op.
        val caster = EntityId.generate()
        val warder = EntityId.generate()
        val spell = EntityId.generate()
        val hand = listOf(EntityId.generate(), EntityId.generate())
        val state = stateWithHandAndSpell(caster, spell, hand)
        val effect = WardCounterEffect(WardCost.Discard(count = 0, random = false))

        val result = executor.execute(state, effect, context(spell, warder))

        result.isSuccess shouldBe true
        // Spell remains on the stack and nothing was discarded.
        result.state.stack shouldContainExactly listOf(spell)
        result.state.getHand(caster) shouldContainExactly hand
        result.state.getZone(caster, Zone.GRAVEYARD) shouldBe emptyList()
    }
})
