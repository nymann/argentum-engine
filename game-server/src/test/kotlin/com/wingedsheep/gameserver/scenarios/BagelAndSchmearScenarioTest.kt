package com.wingedsheep.gameserver.scenarios

import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.gameserver.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Bagel and Schmear.
 *
 * Card reference:
 * - Bagel and Schmear ({1}): Artifact — Food (colorless)
 *   "Share — {W}, {T}, Sacrifice this artifact: Put a +1/+1 counter on up to one target creature,
 *    then draw a card. Activate only as a sorcery."
 *   "Nosh — {2}, {T}, Sacrifice this artifact: You gain 3 life and draw a card."
 */
class BagelAndSchmearScenarioTest : ScenarioTestBase() {

    init {
        context("Bagel and Schmear enters the battlefield") {

            test("casts for {1} and resolves as Artifact — Food permanent") {
                val game = scenario()
                    .withPlayers("Caster", "Opponent")
                    .withCardInHand(1, "Bagel and Schmear")
                    .withLandsOnBattlefield(1, "Plains", 1)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val initialHandSize = game.handSize(1)

                val castResult = game.castSpell(1, "Bagel and Schmear")
                withClue("Casting Bagel and Schmear for {1} should succeed: ${castResult.error}") {
                    castResult.error shouldBe null
                }
                game.resolveStack()

                withClue("Bagel and Schmear should be on the battlefield") {
                    game.isOnBattlefield("Bagel and Schmear") shouldBe true
                }

                withClue("Bagel and Schmear should no longer be in hand") {
                    game.handSize(1) shouldBe initialHandSize - 1
                    game.isInHand(1, "Bagel and Schmear") shouldBe false
                }

                val permanentId = game.findPermanent("Bagel and Schmear")
                val card = permanentId?.let { game.state.getEntity(it)?.get<CardComponent>() }

                withClue("Type line should be 'Artifact — Food'") {
                    card?.typeLine shouldBe "Artifact — Food"
                }

                withClue("Bagel and Schmear should be colorless (no colors)") {
                    card?.colors shouldBe emptySet()
                }

                withClue("Mana cost should be {1} (converted mana cost 1)") {
                    card?.manaCost shouldBe "{1}"
                }
            }
        }
    }
}
