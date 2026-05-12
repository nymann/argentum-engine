package com.wingedsheep.gameserver.scenarios

import com.wingedsheep.engine.mechanics.layers.StateProjector
import com.wingedsheep.gameserver.ScenarioTestBase
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Rhino, Barreling Brute.
 *
 * Card reference:
 * - Rhino, Barreling Brute ({3}{R}{R}{G}{G}): Legendary Creature — Human Villain, 6/7
 *   "Vigilance, trample, haste"
 *   "Whenever Rhino, Barreling Brute attacks, if you've cast a spell with mana value 4 or
 *    greater this turn, draw a card."
 */
class RhinoBarrelingBruteScenarioTest : ScenarioTestBase() {

    private val stateProjector = StateProjector()

    init {
        context("Rhino, Barreling Brute — card definition") {

            test("cast with full cost enters battlefield as a 6/7 Legendary Creature — Human Villain with vigilance, trample, and haste") {
                // {3}{R}{R}{G}{G}: 3 Mountains supply RRR (2 red + 1 generic),
                // 2 Forests supply GG, 2 Islands supply the remaining 2 generic mana.
                val game = scenario()
                    .withPlayers("Active", "Opponent")
                    .withCardInHand(1, "Rhino, Barreling Brute")
                    .withLandsOnBattlefield(1, "Mountain", 3)
                    .withLandsOnBattlefield(1, "Forest", 2)
                    .withLandsOnBattlefield(1, "Island", 2)
                    .withCardInLibrary(1, "Island")
                    .withCardInLibrary(1, "Island")
                    .withCardInLibrary(2, "Island")
                    .withCardInLibrary(2, "Island")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val castResult = game.castSpell(1, "Rhino, Barreling Brute")
                withClue("Casting Rhino, Barreling Brute for {3}{R}{R}{G}{G} should succeed: ${castResult.error}") {
                    castResult.error shouldBe null
                }
                game.resolveStack()

                withClue("Rhino, Barreling Brute should be on the battlefield") {
                    game.isOnBattlefield("Rhino, Barreling Brute") shouldBe true
                }

                val rhinoId = game.findPermanent("Rhino, Barreling Brute")!!
                val projected = stateProjector.project(game.state)

                withClue("Rhino should be a 6/7") {
                    projected.getPower(rhinoId) shouldBe 6
                    projected.getToughness(rhinoId) shouldBe 7
                }

                withClue("Rhino should have vigilance") {
                    projected.hasKeyword(rhinoId, Keyword.VIGILANCE) shouldBe true
                }
                withClue("Rhino should have trample") {
                    projected.hasKeyword(rhinoId, Keyword.TRAMPLE) shouldBe true
                }
                withClue("Rhino should have haste") {
                    projected.hasKeyword(rhinoId, Keyword.HASTE) shouldBe true
                }
            }
        }
    }
}
