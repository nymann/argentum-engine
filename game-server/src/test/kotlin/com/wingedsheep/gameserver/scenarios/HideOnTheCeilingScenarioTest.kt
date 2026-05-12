package com.wingedsheep.gameserver.scenarios

import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.gameserver.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Hide on the Ceiling.
 *
 * Card reference:
 * - Hide on the Ceiling ({X}{U}): Instant
 *   "Exile X target artifacts and/or creatures. Return them to the battlefield under their owners'
 *    control at the beginning of the next end step."
 */
class HideOnTheCeilingScenarioTest : ScenarioTestBase() {

    private fun TestGame.isInExile(playerNumber: Int, cardName: String): Boolean {
        val playerId = if (playerNumber == 1) player1Id else player2Id
        return state.getExile(playerId).any { entityId ->
            state.getEntity(entityId)?.get<CardComponent>()?.name == cardName
        }
    }

    private fun ScenarioBuilder.withLibraryCards(playerNumber: Int, cardName: String, count: Int): ScenarioBuilder {
        repeat(count) { withCardInLibrary(playerNumber, cardName) }
        return this
    }

    init {
        context("Hide on the Ceiling — exile and delayed return") {

            test("exiles target creature on resolution, goes to graveyard, and delayed trigger returns creature at next end step") {
                val game = scenario()
                    .withPlayers("Caster", "Opponent")
                    .withCardInHand(1, "Hide on the Ceiling")
                    .withCardOnBattlefield(2, "Glory Seeker")
                    .withLandsOnBattlefield(1, "Island", 3)
                    .withLibraryCards(1, "Island", 5)
                    .withLibraryCards(2, "Island", 5)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val targetId = game.findPermanent("Glory Seeker")
                val castResult = game.castXSpell(1, "Hide on the Ceiling", 1, targetId)
                withClue("Casting Hide on the Ceiling with X=1 should succeed: ${castResult.error}") {
                    castResult.error shouldBe null
                }
                game.resolveStack()

                withClue("Glory Seeker should be in exile after Hide on the Ceiling resolves") {
                    game.isInExile(2, "Glory Seeker") shouldBe true
                }
                withClue("Glory Seeker should not be on the battlefield") {
                    game.isOnBattlefield("Glory Seeker") shouldBe false
                }
                withClue("Hide on the Ceiling should be in its owner's graveyard") {
                    game.isInGraveyard(1, "Hide on the Ceiling") shouldBe true
                }

                game.passUntilPhase(Phase.ENDING, Step.END)
                game.resolveStack()

                withClue("Delayed trigger should return Glory Seeker to the battlefield at the next end step") {
                    game.isOnBattlefield("Glory Seeker") shouldBe true
                }
            }
        }
    }
}
