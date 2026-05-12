package com.wingedsheep.gameserver.scenarios

import com.wingedsheep.engine.state.components.battlefield.AttachedToComponent
import com.wingedsheep.gameserver.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Scenario tests for Biorganic Carapace.
 *
 * Card reference:
 * - Biorganic Carapace ({2}{W}{U}): Artifact — Equipment
 *   "When this Equipment enters, attach it to target creature you control."
 *   "Equipped creature gets +2/+2."
 *   "Equip {2}"
 */
class BiorganicCarapaceScenarioTest : ScenarioTestBase() {

    init {
        context("Biorganic Carapace ETB auto-attach") {

            test("entering the battlefield attaches to chosen creature and grants +2/+2") {
                val game = scenario()
                    .withPlayers("Caster", "Opponent")
                    .withCardInHand(1, "Biorganic Carapace")
                    .withCardOnBattlefield(1, "Glory Seeker") // vanilla 2/2
                    .withLandsOnBattlefield(1, "Plains", 2)
                    .withLandsOnBattlefield(1, "Island", 2)
                    .withCardInLibrary(1, "Plains")
                    .withCardInLibrary(2, "Island")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val glorySeekerId = game.findPermanent("Glory Seeker")!!

                withClue("Glory Seeker base power should be 2") {
                    game.state.projectedState.getPower(glorySeekerId) shouldBe 2
                }
                withClue("Glory Seeker base toughness should be 2") {
                    game.state.projectedState.getToughness(glorySeekerId) shouldBe 2
                }

                val castResult = game.castSpell(1, "Biorganic Carapace")
                withClue("Casting Biorganic Carapace should succeed: ${castResult.error}") {
                    castResult.error shouldBe null
                }

                // Resolve the spell; the ETB trigger fires and waits for target selection
                game.resolveStack()

                withClue("ETB trigger should present a target decision") {
                    game.hasPendingDecision() shouldBe true
                }

                game.selectTargets(listOf(glorySeekerId))
                game.resolveStack()

                withClue("Biorganic Carapace should be on the battlefield") {
                    game.isOnBattlefield("Biorganic Carapace") shouldBe true
                }

                val carapaceId = game.findPermanent("Biorganic Carapace")!!
                val attachedTo = game.state.getEntity(carapaceId)!!.get<AttachedToComponent>()
                withClue("Biorganic Carapace should be attached to Glory Seeker") {
                    attachedTo shouldNotBe null
                    attachedTo!!.targetId shouldBe glorySeekerId
                }

                withClue("Equipped Glory Seeker power should be 4 (2 + 2)") {
                    game.state.projectedState.getPower(glorySeekerId) shouldBe 4
                }
                withClue("Equipped Glory Seeker toughness should be 4 (2 + 2)") {
                    game.state.projectedState.getToughness(glorySeekerId) shouldBe 4
                }

                withClue("Biorganic Carapace should no longer be in hand") {
                    game.isInHand(1, "Biorganic Carapace") shouldBe false
                }
            }
        }
    }
}
