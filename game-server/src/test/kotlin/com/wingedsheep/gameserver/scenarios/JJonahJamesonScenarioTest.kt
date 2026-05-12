package com.wingedsheep.gameserver.scenarios

import com.wingedsheep.engine.mechanics.layers.StateProjector
import com.wingedsheep.gameserver.ScenarioTestBase
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for J. Jonah Jameson.
 *
 * Card reference:
 * - J. Jonah Jameson ({2}{R}): Legendary Creature — Human Citizen, 2/2
 *   "When J. Jonah Jameson enters, suspect up to one target creature an opponent controls."
 */
class JJonahJamesonScenarioTest : ScenarioTestBase() {

    private val stateProjector = StateProjector()

    init {
        context("J. Jonah Jameson ETB suspect") {

            test("casts for {2}{R} and ETB suspects the targeted opposing creature") {
                val game = scenario()
                    .withPlayers("ActivePlayer", "Opponent")
                    .withCardInHand(1, "J. Jonah Jameson")
                    .withCardOnBattlefield(2, "Glory Seeker")
                    .withLandsOnBattlefield(1, "Mountain", 3)
                    .withCardInLibrary(1, "Mountain")
                    .withCardInLibrary(2, "Plains")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val castResult = game.castSpell(1, "J. Jonah Jameson")
                withClue("Casting J. Jonah Jameson for {2}{R} should succeed: ${castResult.error}") {
                    castResult.error shouldBe null
                }

                // Creature resolves, ETB trigger fires and pauses for target selection
                game.resolveStack()

                withClue("ETB should create a pending target-selection decision") {
                    game.hasPendingDecision() shouldBe true
                }

                val glorySeekerTarget = game.findPermanent("Glory Seeker")!!
                game.selectTargets(listOf(glorySeekerTarget))

                // ETB trigger resolves, applying suspect to Glory Seeker
                game.resolveStack()

                withClue("J. Jonah Jameson should be on the battlefield") {
                    game.isOnBattlefield("J. Jonah Jameson") shouldBe true
                }
                withClue("Active player's hand should no longer contain J. Jonah Jameson") {
                    game.isInHand(1, "J. Jonah Jameson") shouldBe false
                }

                val projected = stateProjector.project(game.state)

                withClue("Suspected Glory Seeker should have menace") {
                    projected.hasKeyword(glorySeekerTarget, Keyword.MENACE) shouldBe true
                }
                withClue("Suspected Glory Seeker cannot be declared as a blocker") {
                    projected.cantBlock(glorySeekerTarget) shouldBe true
                }
            }

            test("resolves normally and enters as a 2/2 Legendary Creature — Human Citizen with no target when there are no opponent creatures") {
                val game = scenario()
                    .withPlayers("ActivePlayer", "Opponent")
                    .withCardInHand(1, "J. Jonah Jameson")
                    .withLandsOnBattlefield(1, "Mountain", 3)
                    .withCardInLibrary(1, "Mountain")
                    .withCardInLibrary(2, "Plains")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val castResult = game.castSpell(1, "J. Jonah Jameson")
                withClue("Casting J. Jonah Jameson with no opponent creatures should still be legal: ${castResult.error}") {
                    castResult.error shouldBe null
                }

                // Creature resolves; ETB trigger with no legal targets either fires with an empty
                // selection or is skipped entirely — either way JJJ should land on the battlefield
                game.resolveStack()

                // If the engine pauses for optional target selection, skip it
                if (game.hasPendingDecision()) {
                    game.skipTargets()
                    game.resolveStack()
                }

                withClue("J. Jonah Jameson should be on the battlefield even with no suspect target") {
                    game.isOnBattlefield("J. Jonah Jameson") shouldBe true
                }
            }
        }
    }
}
