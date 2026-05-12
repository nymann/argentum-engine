package com.wingedsheep.gameserver.scenarios

import com.wingedsheep.gameserver.ScenarioTestBase
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Inner Demons Gangsters.
 *
 * Card reference:
 * - Inner Demons Gangsters ({3}{B}): Creature — Human Rogue Villain, 3/4
 *   "{B}, Discard a card: This creature gets +1/+0 and gains menace until end of turn.
 *    Activate only as a sorcery."
 */
class InnerDemonsGangstersScenarioTest : ScenarioTestBase() {

    init {
        context("Inner Demons Gangsters — enters the battlefield") {

            test("casts for {3}{B} and enters as a 3/4 Human Rogue Villain with no default menace") {
                val game = scenario()
                    .withPlayers("Caster", "Opponent")
                    .withCardInHand(1, "Inner Demons Gangsters")
                    .withLandsOnBattlefield(1, "Swamp", 1)
                    .withLandsOnBattlefield(1, "Plains", 3)
                    .withCardInLibrary(1, "Island")
                    .withCardInLibrary(2, "Island")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val castResult = game.castSpell(1, "Inner Demons Gangsters")
                withClue("Casting Inner Demons Gangsters for {3}{B} should succeed: ${castResult.error}") {
                    castResult.error shouldBe null
                }
                game.resolveStack()

                withClue("Inner Demons Gangsters should be on the battlefield") {
                    game.isOnBattlefield("Inner Demons Gangsters") shouldBe true
                }

                val gangsterId = game.findPermanent("Inner Demons Gangsters")!!
                val clientState = game.getClientState(1)
                val cardInfo = clientState.cards[gangsterId]!!

                withClue("Inner Demons Gangsters should be a 3/4") {
                    cardInfo.power shouldBe 3
                    cardInfo.toughness shouldBe 4
                }

                withClue("Inner Demons Gangsters type line should include Human, Rogue, and Villain subtypes") {
                    cardInfo.subtypes shouldBe setOf("Human", "Rogue", "Villain")
                }

                withClue("Inner Demons Gangsters should not have menace by default") {
                    cardInfo.keywords.contains(Keyword.MENACE) shouldBe false
                }
            }
        }
    }
}
