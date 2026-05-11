package com.wingedsheep.gameserver.scenarios

import com.wingedsheep.gameserver.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario test for Romantic Rendezvous.
 *
 * Card reference:
 * - Romantic Rendezvous ({1}{R}): Sorcery
 *   "Discard a card, then draw two cards."
 */
class RomanticRendezvousScenarioTest : ScenarioTestBase() {

    init {
        context("Romantic Rendezvous — discard a card, draw two") {

            test("discards one card then draws two after resolution") {
                // Starting hand: [Romantic Rendezvous, Grizzly Bears, Glory Seeker] = 3 cards
                // Expected hand after: 3 - 1 (cast RR) - 1 (discard GB) + 2 (draw) = 3 cards
                val game = scenario()
                    .withPlayers("Caster", "Opponent")
                    .withCardInHand(1, "Romantic Rendezvous")
                    .withCardInHand(1, "Grizzly Bears")
                    .withCardInHand(1, "Glory Seeker")
                    .withLandsOnBattlefield(1, "Mountain", 2)
                    .withCardInLibrary(1, "Island")
                    .withCardInLibrary(1, "Plains")
                    .withCardInLibrary(2, "Island")
                    .withCardInLibrary(2, "Plains")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val initialOpponentHandSize = game.handSize(2)
                val initialOpponentLibrarySize = game.librarySize(2)
                val initialOpponentLife = game.getLifeTotal(2)

                val castResult = game.castSpell(1, "Romantic Rendezvous")
                withClue("Casting Romantic Rendezvous should succeed: ${castResult.error}") {
                    castResult.error shouldBe null
                }

                // Spell resolves; engine presents a SelectCardsDecision for the discard
                game.resolveStack()

                withClue("Engine should present a discard selection decision") {
                    game.hasPendingDecision() shouldBe true
                }

                // Player 1 discards Grizzly Bears
                val bearInHand = game.findCardsInHand(1, "Grizzly Bears")
                withClue("Grizzly Bears should still be in hand when discard decision is presented") {
                    bearInHand.isNotEmpty() shouldBe true
                }
                game.selectCards(bearInHand)

                // After discard, the engine automatically draws two cards
                withClue("Romantic Rendezvous should be in the caster's graveyard after resolution") {
                    game.isInGraveyard(1, "Romantic Rendezvous") shouldBe true
                }
                withClue("Grizzly Bears should be in the caster's graveyard (discarded)") {
                    game.isInGraveyard(1, "Grizzly Bears") shouldBe true
                }
                withClue("Hand size should be 3: started 3, cast RR (−1), discard GB (−1), draw two (+2)") {
                    game.handSize(1) shouldBe 3
                }
                withClue("Opponent's hand should be unchanged") {
                    game.handSize(2) shouldBe initialOpponentHandSize
                }
                withClue("Opponent's library should be unchanged") {
                    game.librarySize(2) shouldBe initialOpponentLibrarySize
                }
                withClue("Opponent's life total should be unchanged") {
                    game.getLifeTotal(2) shouldBe initialOpponentLife
                }
            }
        }
    }
}
