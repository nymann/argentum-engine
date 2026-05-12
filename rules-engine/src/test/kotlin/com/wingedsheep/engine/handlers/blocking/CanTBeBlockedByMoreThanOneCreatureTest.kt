package com.wingedsheep.engine.handlers.blocking

import com.wingedsheep.engine.core.DeclareBlockers
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.Deck
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContainIgnoringCase

/**
 * BDD test: a creature bearing the cant-be-blocked-by-more-than-one restriction
 * must accept a single-blocker assignment but reject a two-blocker assignment.
 *
 * GIVEN  Attacker A has CanTBeBlockedByMoreThanOneCreature attached (the primitive)
 * AND    Player 2 controls B1 and B2, both able to block A
 * AND    A is declared as an attacker
 * WHEN   Player 2 attempts to declare both B1 and B2 as blockers for A
 * THEN   {B1 -> A} is accepted
 * AND    {B1 -> A, B2 -> A} is rejected with a "more than one" error
 */
class CanTBeBlockedByMoreThanOneCreatureTest : FunSpec({

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

    fun GameTestDriver.advanceToPlayer1DeclareAttackers() {
        passPriorityUntil(Step.DECLARE_ATTACKERS)
        var safety = 0
        while (activePlayer != player1 && safety < 50) {
            bothPass()
            passPriorityUntil(Step.DECLARE_ATTACKERS)
            safety++
        }
    }

    test("single-blocker assignment is legal against an attacker with the cant-be-blocked-by-more-than-one restriction") {
        val driver = createDriver()

        val attacker = driver.putCreatureOnBattlefield(driver.player1, "Grizzly Bears")
        driver.removeSummoningSickness(attacker)
        driver.replaceState(driver.state.updateEntity(attacker) { it.with(CanTBeBlockedByMoreThanOneCreature) })

        val blocker = driver.putCreatureOnBattlefield(driver.player2, "Grizzly Bears")
        driver.removeSummoningSickness(blocker)

        driver.advanceToPlayer1DeclareAttackers()
        driver.currentStep shouldBe Step.DECLARE_ATTACKERS

        driver.declareAttackers(driver.player1, listOf(attacker), driver.player2)
            .isSuccess shouldBe true

        driver.bothPass()
        driver.currentStep shouldBe Step.DECLARE_BLOCKERS

        driver.declareBlockers(driver.player2, mapOf(blocker to listOf(attacker)))
            .isSuccess shouldBe true
    }

    test("second-blocker assignment is illegal against an attacker with the cant-be-blocked-by-more-than-one restriction") {
        val driver = createDriver()

        val attacker = driver.putCreatureOnBattlefield(driver.player1, "Grizzly Bears")
        driver.removeSummoningSickness(attacker)
        driver.replaceState(driver.state.updateEntity(attacker) { it.with(CanTBeBlockedByMoreThanOneCreature) })

        val blocker1 = driver.putCreatureOnBattlefield(driver.player2, "Grizzly Bears")
        val blocker2 = driver.putCreatureOnBattlefield(driver.player2, "Grizzly Bears")
        driver.removeSummoningSickness(blocker1)
        driver.removeSummoningSickness(blocker2)

        driver.advanceToPlayer1DeclareAttackers()
        driver.currentStep shouldBe Step.DECLARE_ATTACKERS

        driver.declareAttackers(driver.player1, listOf(attacker), driver.player2)
            .isSuccess shouldBe true

        driver.bothPass()
        driver.currentStep shouldBe Step.DECLARE_BLOCKERS

        val result = driver.submitExpectFailure(
            DeclareBlockers(
                driver.player2,
                mapOf(
                    blocker1 to listOf(attacker),
                    blocker2 to listOf(attacker)
                )
            )
        )
        result.error shouldContainIgnoringCase "more than one"
    }
})
