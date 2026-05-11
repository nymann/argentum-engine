package com.wingedsheep.engine.handlers.triggers

import com.wingedsheep.engine.state.components.battlefield.CountersComponent
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.sdk.core.CounterType
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.scripting.GameEvent
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.TriggerBinding
import com.wingedsheep.sdk.scripting.TriggerSpec
import com.wingedsheep.sdk.scripting.events.DamageType
import com.wingedsheep.sdk.scripting.events.RecipientFilter
import com.wingedsheep.sdk.scripting.predicates.CardPredicate
import com.wingedsheep.sdk.scripting.predicates.ControllerPredicate
import com.wingedsheep.sdk.scripting.predicates.StatePredicate
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * BDD specification for the "whenever a modified creature you control deals combat damage
 * to a player" engine trigger (Rule 700.4).
 *
 * A creature is "modified" if it has one or more counters, has one or more Equipment
 * attached, or is enchanted by one or more Auras its controller controls.
 *
 * The test registers a watcher card that gains 1 life for its controller each time the
 * trigger fires, then verifies the trigger fires exactly once — for the modified attacker —
 * and not for the unmodified attacker.
 */
class WheneverAModifiedCreatureYouControlDealsCombatDamageToAPlayerTest : FunSpec({

    val modifiedCreatureYouControl = GameObjectFilter(
        cardPredicates = listOf(CardPredicate.IsCreature),
        statePredicates = listOf(StatePredicate.IsModified),
        controllerPredicate = ControllerPredicate.ControlledByYou
    )

    // A watcher card whose triggered ability fires each time a modified creature
    // its controller controls deals combat damage to a player.  Each firing gains
    // the controller 1 life, giving an observable count of trigger firings.
    val TriggerRecorder = card("Modified Creature Trigger Recorder") {
        typeLine = "Creature — Human"
        power = 0
        toughness = 4

        triggeredAbility {
            trigger = TriggerSpec(
                event = GameEvent.DealsDamageEvent(
                    damageType = DamageType.Combat,
                    recipient = RecipientFilter.AnyPlayer,
                    sourceFilter = modifiedCreatureYouControl
                ),
                binding = TriggerBinding.ANY
            )
            effect = Effects.GainLife(1)
        }
    }

    fun createDriver(): GameTestDriver {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all + listOf(TriggerRecorder))
        return driver
    }

    fun GameTestDriver.advanceToPlayer1DeclareAttackers() {
        passPriorityUntil(Step.DECLARE_ATTACKERS)
        var safety = 0
        while (activePlayer != player1 && safety < 50) {
            bothPass()
            passPriorityUntil(Step.DECLARE_ATTACKERS)
            safety++
        }
    }

    test("trigger fires exactly once — for the attacker with a +1/+1 counter, not for the unmodified attacker") {
        val driver = createDriver()
        driver.initMirrorMatch(
            deck = Deck.of("Forest" to 40),
            startingLife = 20
        )

        val attacker = driver.player1
        val defender = driver.player2

        // The watcher stays back and records trigger firings via life-gain.
        driver.putCreatureOnBattlefield(attacker, "Modified Creature Trigger Recorder")

        // Two Grizzly Bears: one with a +1/+1 counter (modified), one plain (unmodified).
        val modifiedAttacker = driver.putCreatureOnBattlefield(attacker, "Grizzly Bears")
        val unmodifiedAttacker = driver.putCreatureOnBattlefield(attacker, "Grizzly Bears")
        driver.removeSummoningSickness(modifiedAttacker)
        driver.removeSummoningSickness(unmodifiedAttacker)

        // Give one attacker a +1/+1 counter — qualifies it as "modified" per Rule 700.4.
        driver.replaceState(
            driver.state.updateEntity(modifiedAttacker) { container ->
                val counters = container.get<CountersComponent>() ?: CountersComponent()
                container.with(counters.withAdded(CounterType.PLUS_ONE_PLUS_ONE, 1))
            }
        )

        driver.advanceToPlayer1DeclareAttackers()

        // Both creatures attack; defender has no blockers.
        driver.declareAttackers(attacker, listOf(modifiedAttacker, unmodifiedAttacker), defender)

        // Advance through all remaining combat steps (including damage assignment,
        // damage dealing, and trigger resolution) until postcombat main phase.
        driver.passPriorityUntil(Step.POSTCOMBAT_MAIN)

        // The trigger should have fired exactly once (modified attacker only).
        // Each firing gains the controller 1 life: 20 + 1 = 21.
        driver.getLifeTotal(attacker) shouldBe 21
    }
})
