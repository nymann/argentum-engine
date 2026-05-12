package com.wingedsheep.gameserver.scenarios

import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.gameserver.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Eerie Gravestone.
 *
 * Card reference:
 * - Eerie Gravestone ({2}): Artifact
 *   "When this artifact enters, draw a card."
 *   "{1}{B}, Sacrifice Eerie Gravestone: Mill four cards.
 *    Then you may put a creature card milled this way into your hand."
 */
class EerieGravestoneScenarioTest : ScenarioTestBase() {

    init {
        context("Eerie Gravestone cast") {

            test("enters the battlefield as an Artifact after being cast for {2}") {
                val game = scenario()
                    .withPlayers("Caster", "Opponent")
                    .withCardInHand(1, "Eerie Gravestone")
                    .withLandsOnBattlefield(1, "Swamp", 2)
                    .withCardInLibrary(1, "Island")
                    .withCardInLibrary(2, "Island")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val castResult = game.castSpell(1, "Eerie Gravestone")
                withClue("Casting Eerie Gravestone for {2} should succeed: ${castResult.error}") {
                    castResult.error shouldBe null
                }
                game.resolveStack()

                withClue("Eerie Gravestone should be on the battlefield") {
                    game.isOnBattlefield("Eerie Gravestone") shouldBe true
                }

                val entityId = game.findPermanent("Eerie Gravestone")
                val cardComp = game.state.getEntity(entityId!!)?.get<CardComponent>()
                withClue("Eerie Gravestone should be recognized as an Artifact") {
                    cardComp?.typeLine?.isArtifact shouldBe true
                }

                withClue("Eerie Gravestone should no longer be in the active player's hand") {
                    game.isInHand(1, "Eerie Gravestone") shouldBe false
                }
            }
        }
    }
}
