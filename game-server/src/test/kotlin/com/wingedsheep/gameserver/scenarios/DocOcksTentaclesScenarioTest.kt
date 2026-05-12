package com.wingedsheep.gameserver.scenarios

import com.wingedsheep.engine.state.components.battlefield.AttachedToComponent
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.gameserver.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Doc Ock's Tentacles.
 *
 * Card reference:
 * - Doc Ock's Tentacles ({1}): Artifact — Equipment
 *   "Equipped creature gets +4/+4."
 *   "Whenever a creature with mana value 5 or greater enters the battlefield under your control,
 *    you may attach this Equipment to it."
 *   "Equip {5}"
 */
class DocOcksTentaclesScenarioTest : ScenarioTestBase() {

    init {
        context("Doc Ock's Tentacles enters the battlefield") {

            test("enters as an unattached Artifact — Equipment when cast for {1}") {
                val game = scenario()
                    .withPlayers("Player", "Opponent")
                    .withCardInHand(1, "Doc Ock's Tentacles")
                    .withLandsOnBattlefield(1, "Mountain", 1)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val castResult = game.castSpell(1, "Doc Ock's Tentacles")
                withClue("Casting Doc Ock's Tentacles for {1} should succeed: ${castResult.error}") {
                    castResult.error shouldBe null
                }
                game.resolveStack()

                withClue("Doc Ock's Tentacles should be on the battlefield") {
                    game.isOnBattlefield("Doc Ock's Tentacles") shouldBe true
                }

                val tentaclesId = game.findPermanent("Doc Ock's Tentacles")!!
                val entity = game.state.getEntity(tentaclesId)!!

                withClue("Doc Ock's Tentacles should be Artifact — Equipment") {
                    entity.get<CardComponent>()?.typeLine shouldBe "Artifact — Equipment"
                }

                withClue("Doc Ock's Tentacles should be unattached on entry") {
                    entity.get<AttachedToComponent>() shouldBe null
                }
            }
        }
    }
}
