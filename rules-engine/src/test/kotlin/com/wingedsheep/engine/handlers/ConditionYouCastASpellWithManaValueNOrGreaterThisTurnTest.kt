package com.wingedsheep.engine.handlers

import com.wingedsheep.engine.state.CastSpellRecord
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.sdk.core.CardType
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.core.TypeLine
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.scripting.conditions.YouCastASpellWithManaValueNOrGreaterThisTurn
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * BDD: YouCastASpellWithManaValueNOrGreaterThisTurn condition
 *
 * GIVEN a per-player, per-turn tracker of the highest mana value cast
 * WHEN evaluated for a player at threshold N
 * THEN it is true iff that player has cast at least one spell with mana value >= N
 *   this turn, ignoring other players' spells, and resets at end of turn.
 */
class ConditionYouCastASpellWithManaValueNOrGreaterThisTurnTest : FunSpec({

    test("predicate is player-scoped, threshold-sensitive, and resets at end of turn") {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all)
        driver.initMirrorMatch(
            deck = Deck.of("Forest" to 40),
            skipMulligans = true
        )
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        val active = driver.activePlayer!!
        val opponent = driver.getOpponent(active)

        val evaluator = ConditionEvaluator()

        fun evalFor(threshold: Int): Boolean {
            val context = EffectContext(
                sourceId = null,
                controllerId = active,
                opponentId = opponent
            )
            return evaluator.evaluate(
                driver.state,
                YouCastASpellWithManaValueNOrGreaterThisTurn(threshold),
                context
            )
        }

        // GIVEN: opponent has cast a spell with MV=5 this turn; active player has cast none.
        val mv5Record = CastSpellRecord(
            typeLine = TypeLine.instant(),
            manaValue = 5,
            colors = emptySet(),
            isFaceDown = false
        )
        driver.replaceState(
            driver.state.copy(
                spellsCastThisTurnByPlayer = mapOf(opponent to listOf(mv5Record))
            )
        )

        // THEN: before the active player casts anything, predicate is false at N=4.
        // The opponent's MV=5 spell must NOT satisfy the active player's condition.
        evalFor(4) shouldBe false

        // WHEN: the active player casts a spell with mana value 4 (injected directly).
        val mv4Record = CastSpellRecord(
            typeLine = TypeLine(cardTypes = setOf(CardType.SORCERY)),
            manaValue = 4,
            colors = emptySet(),
            isFaceDown = false
        )
        driver.replaceState(
            driver.state.copy(
                spellsCastThisTurnByPlayer = driver.state.spellsCastThisTurnByPlayer +
                    (active to listOf(mv4Record))
            )
        )

        // THEN: predicate is true at N=4, true at N=3, but false at N=5.
        evalFor(4) shouldBe true
        evalFor(3) shouldBe true
        evalFor(5) shouldBe false

        // WHEN: the active player's turn ends and a new turn begins.
        driver.passPriorityUntil(Step.END, maxPasses = 200)
        driver.bothPass()
        // Sanity: turn has advanced to the opponent.
        driver.activePlayer shouldBe opponent

        // THEN: the tracker resets; predicate is false at N=4 for the original active player.
        evalFor(4) shouldBe false
    }
})
