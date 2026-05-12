package com.wingedsheep.engine.handlers.effects

import com.wingedsheep.engine.core.SelectCardsDecision
import com.wingedsheep.engine.state.Component
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.Deck
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * BDD specification: a static continuous effect on the battlefield that raises
 * a player's maximum hand size from the rule 402.2 default of 7 to 8 must be
 * respected by the cleanup step's discard-to-maximum-hand-size enforcement.
 */
class ModifyMaximumHandSizeTest : FunSpec({

    fun createDriver(): GameTestDriver {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all)
        driver.initMirrorMatch(
            deck = Deck.of(
                "Forest" to 20,
                "Grizzly Bears" to 20
            ),
            skipMulligans = true
        )
        return driver
    }

    test("static effect raising max hand size to 8 requires discarding exactly 1 card when player holds 9") {
        val driver = createDriver()
        val activePlayer = driver.activePlayer!!

        // GIVEN: a player whose default maximum hand size is 7 (rule 402.2)
        driver.getHandSize(activePlayer) shouldBe 7

        // AND: a static continuous effect on the battlefield sets that player's max hand size to 8.
        // The effect is represented by MaximumHandSizeComponent injected directly onto the player
        // entity, simulating what ModifyMaximumHandSizeEffect will write when it is wired into the
        // cleanup phase.  Production code that reads this component does not exist yet, so the
        // test is red: CleanupPhaseManager still uses the hardcoded default of 7.
        driver.replaceState(
            driver.state.updateEntity(activePlayer) { container ->
                container.with(MaximumHandSizeComponent(8))
            }
        )

        // AND: the player has 9 cards in hand at the start of the cleanup step
        val extra1 = driver.putCardInHand(activePlayer, "Grizzly Bears")
        val extra2 = driver.putCardInHand(activePlayer, "Grizzly Bears")
        driver.getHandSize(activePlayer) shouldBe 9

        // WHEN: the cleanup step's discard-to-maximum-hand-size action is processed
        driver.passPriorityUntil(Step.END)
        driver.bothPass()

        // THEN: the engine treats the player's maximum hand size as 8, not 7 …
        driver.isPaused shouldBe true
        val decision = driver.pendingDecision
        decision.shouldBeInstanceOf<SelectCardsDecision>()
        decision.playerId shouldBe activePlayer

        // … AND the player is required to discard exactly 1 card (9 − 8), not 2 (9 − 7)
        decision.minSelections shouldBe 1
        decision.maxSelections shouldBe 1

        // … AND after cleanup the player has exactly 8 cards in hand
        driver.submitCardSelection(activePlayer, listOf(extra1))
        driver.getHandSize(activePlayer) shouldBe 8
    }
})

/**
 * Stub component that overrides a player's maximum hand size.
 *
 * Defined here so the test compiles before production code lands.  When
 * MaximumHandSizeComponent is added to PlayerComponents in the rules-engine,
 * this local definition will be replaced by an import of the production class.
 */
private data class MaximumHandSizeComponent(val maxSize: Int) : Component
