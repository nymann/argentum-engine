package com.wingedsheep.gameserver.scenarios

import com.wingedsheep.engine.state.components.battlefield.TappedComponent
import com.wingedsheep.engine.state.components.player.ManaPoolComponent
import com.wingedsheep.gameserver.ScenarioTestBase
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for City Pigeon.
 *
 * Card reference:
 * - City Pigeon ({W}): Creature — Bird, 1/1
 *   "Flying"
 *   "When City Pigeon leaves the battlefield, create a Food token."
 */
class CityPigeonScenarioTest : ScenarioTestBase() {

    init {
        context("City Pigeon cast") {

            test("resolves onto the battlefield as a 1/1 white Bird with flying when cast for {W}") {
                val game = scenario()
                    .withPlayers("Active", "Opponent")
                    .withCardInHand(1, "City Pigeon")
                    .withCardOnBattlefield(1, "Plains")
                    .withCardInLibrary(1, "Plains")
                    .withCardInLibrary(2, "Plains")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val plainsId = game.findPermanent("Plains")!!

                val castResult = game.castSpell(1, "City Pigeon")
                withClue("Casting City Pigeon for {W} should succeed: ${castResult.error}") {
                    castResult.error shouldBe null
                }
                game.resolveStack()

                withClue("City Pigeon should be on the battlefield") {
                    game.isOnBattlefield("City Pigeon") shouldBe true
                }

                val pigeonId = game.findPermanent("City Pigeon")!!
                val clientState = game.getClientState(1)
                val pigeonInfo = clientState.cards[pigeonId]

                withClue("City Pigeon should be a 1/1") {
                    pigeonInfo!!.power shouldBe 1
                    pigeonInfo.toughness shouldBe 1
                }

                withClue("City Pigeon should be white") {
                    pigeonInfo!!.colors shouldBe setOf(Color.WHITE)
                }

                withClue("City Pigeon should have the Bird subtype") {
                    pigeonInfo!!.subtypes.contains("Bird") shouldBe true
                }

                withClue("City Pigeon should have flying") {
                    pigeonInfo!!.keywords.contains(Keyword.FLYING) shouldBe true
                }

                withClue("The Plains should be tapped after paying {W}") {
                    game.state.getEntity(plainsId)?.has<TappedComponent>() shouldBe true
                }

                withClue("The active player's mana pool should be empty after casting") {
                    val manaPool = game.state.getEntity(game.player1Id)?.get<ManaPoolComponent>()
                    manaPool?.white shouldBe 0
                    manaPool?.blue shouldBe 0
                    manaPool?.black shouldBe 0
                    manaPool?.red shouldBe 0
                    manaPool?.green shouldBe 0
                    manaPool?.colorless shouldBe 0
                }
            }
        }
    }
}
