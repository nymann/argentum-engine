package com.wingedsheep.gameserver.scenarios

import com.wingedsheep.gameserver.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

class BeholdTheSinisterSixScenarioTest : ScenarioTestBase() {

    private fun ScenarioBuilder.withLibraryCards(playerNumber: Int, cardName: String, count: Int): ScenarioBuilder {
        repeat(count) { withCardInLibrary(playerNumber, cardName) }
        return this
    }

    init {
        context("Behold the Sinister Six! reanimation") {

            test("costs {6}{B} and returns up to six distinct-name creatures from graveyard to battlefield") {
                val creatureNames = listOf(
                    "Enormous Baloth",
                    "Fire Elemental",
                    "Glory Seeker",
                    "Grizzly Bears",
                    "Jungle Lion",
                    "Hill Giant",
                )

                val game = scenario()
                    .withPlayers("Caster", "Opponent")
                    .withCardInHand(1, "Behold the Sinister Six!")
                    .withCardInGraveyard(1, "Enormous Baloth")
                    .withCardInGraveyard(1, "Fire Elemental")
                    .withCardInGraveyard(1, "Glory Seeker")
                    .withCardInGraveyard(1, "Grizzly Bears")
                    .withCardInGraveyard(1, "Jungle Lion")
                    .withCardInGraveyard(1, "Hill Giant")
                    .withLandsOnBattlefield(1, "Swamp", 7)
                    .withLibraryCards(1, "Island", 5)
                    .withLibraryCards(2, "Island", 5)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val targetIds = creatureNames.flatMap { game.findCardsInGraveyard(1, it) }

                val castResult = game.castSpell(1, "Behold the Sinister Six!")
                withClue("Casting Behold the Sinister Six! should succeed: ${castResult.error}") {
                    castResult.error shouldBe null
                }

                game.resolveStack()
                game.selectCards(targetIds)
                game.resolveStack()

                creatureNames.forEach { name ->
                    withClue("$name should be on the battlefield") {
                        game.isOnBattlefield(name) shouldBe true
                    }
                }
                withClue("Behold the Sinister Six! should be in the caster's graveyard after resolution") {
                    game.isInGraveyard(1, "Behold the Sinister Six!") shouldBe true
                }
                withClue("Caster's graveyard should contain only Behold the Sinister Six! (the six creatures moved to battlefield)") {
                    game.graveyardSize(1) shouldBe 1
                }
            }
        }
    }
}
