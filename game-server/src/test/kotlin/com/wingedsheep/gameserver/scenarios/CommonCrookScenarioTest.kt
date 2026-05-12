package com.wingedsheep.gameserver.scenarios

import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.gameserver.ScenarioTestBase
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Common Crook.
 *
 * Card reference:
 * - Common Crook ({1}{B}): Creature — Human Rogue Villain, 2/2
 *   "When Common Crook dies, create a Treasure token."
 */
class CommonCrookScenarioTest : ScenarioTestBase() {

    init {
        context("Common Crook — casting") {

            test("enters the battlefield as a 2/2 black Human Rogue Villain when cast for {1}{B}") {
                val game = scenario()
                    .withPlayers("ActivePlayer", "Opponent")
                    .withCardInHand(1, "Common Crook")
                    .withLandsOnBattlefield(1, "Swamp", 2)
                    .withCardInLibrary(1, "Swamp")
                    .withCardInLibrary(2, "Swamp")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val castResult = game.castSpell(1, "Common Crook")
                withClue("Casting Common Crook should succeed: ${castResult.error}") {
                    castResult.error shouldBe null
                }
                game.resolveStack()

                withClue("Common Crook should be on the battlefield") {
                    game.isOnBattlefield("Common Crook") shouldBe true
                }
                withClue("Active player's hand should no longer contain Common Crook") {
                    game.isInHand(1, "Common Crook") shouldBe false
                }

                val crookId = game.findPermanent("Common Crook")!!
                val cardComp = game.state.getEntity(crookId)?.get<CardComponent>()!!

                withClue("Common Crook should be a 2/2") {
                    cardComp.baseStats?.basePower shouldBe 2
                    cardComp.baseStats?.baseToughness shouldBe 2
                }
                withClue("Common Crook should be black") {
                    cardComp.colors shouldBe setOf(Color.BLACK)
                }
                withClue("Common Crook should have subtypes Human, Rogue, and Villain") {
                    val subtypeNames = cardComp.typeLine.subtypes.map { it.value }
                    subtypeNames shouldContain "Human"
                    subtypeNames shouldContain "Rogue"
                    subtypeNames shouldContain "Villain"
                }
            }
        }
    }
}
