package com.wingedsheep.gameserver.scenarios

import com.wingedsheep.engine.state.components.player.ManaPoolComponent
import com.wingedsheep.gameserver.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Shock.
 *
 * Card reference:
 * - Shock ({R}): Instant
 *   "Shock deals 2 damage to any target."
 */
class ShockScenarioTest : ScenarioTestBase() {

    init {
        context("Shock — deals 2 damage to any target") {

            test("deals 2 damage to target creature, killing a 2/2 and going to graveyard") {
                val game = scenario()
                    .withPlayers("Caster", "Opponent")
                    .withCardInHand(1, "Shock")
                    .withCardOnBattlefield(2, "Glory Seeker") // 2/2 Human Soldier
                    .withLandsOnBattlefield(1, "Mountain", 1)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val targetId = game.findPermanent("Glory Seeker")!!

                val castResult = game.castSpell(1, "Shock", targetId)
                withClue("Casting Shock should succeed: ${castResult.error}") {
                    castResult.error shouldBe null
                }

                game.resolveStack()

                withClue("Shock should be in the caster's graveyard after resolution") {
                    game.isInGraveyard(1, "Shock") shouldBe true
                }
                withClue("Glory Seeker (2/2) should be destroyed by lethal damage and in opponent's graveyard") {
                    game.isOnBattlefield("Glory Seeker") shouldBe false
                    game.isInGraveyard(2, "Glory Seeker") shouldBe true
                }
                withClue("Active player's mana pool should be empty after casting") {
                    val manaPool = game.state.getEntity(game.player1Id)?.get<ManaPoolComponent>()
                    manaPool?.red shouldBe 0
                    manaPool?.blue shouldBe 0
                    manaPool?.black shouldBe 0
                    manaPool?.green shouldBe 0
                    manaPool?.white shouldBe 0
                    manaPool?.colorless shouldBe 0
                }
            }
        }
    }
}
