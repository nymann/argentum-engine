package com.wingedsheep.gameserver.scenarios

import com.wingedsheep.gameserver.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Aunt May.
 *
 * Card reference:
 * - Aunt May ({W}): Legendary Creature — Human Citizen, 0/2
 *   "Whenever another creature you control enters, you gain 1 life.
 *    If that creature is a Spider, put a +1/+1 counter on it."
 */
class AuntMayScenarioTest : ScenarioTestBase() {

    init {
        context("Cast Aunt May for {W} — forces card-definition path") {

            test("Aunt May resolves, enters battlefield as a 0/2 Legendary Human Citizen, and does not trigger her own ETB ability") {
                val game = scenario()
                    .withPlayers("Caster", "Opponent")
                    .withCardInHand(1, "Aunt May")
                    .withLandsOnBattlefield(1, "Plains", 1)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val initialLifeTotal = game.getLifeTotal(1)

                val castResult = game.castSpell(1, "Aunt May")
                withClue("Casting Aunt May for {W} with one Plains should succeed: ${castResult.error}") {
                    castResult.error shouldBe null
                }
                game.resolveStack()

                withClue("Aunt May should be on the battlefield") {
                    game.isOnBattlefield("Aunt May") shouldBe true
                }

                withClue("Aunt May entering should not trigger her own ETB ability (she is not 'another' creature)") {
                    game.getLifeTotal(1) shouldBe initialLifeTotal
                }

                val cardDef = cardRegistry.getCard("Aunt May")
                withClue("Aunt May should cost {W}") {
                    cardDef?.manaCost?.toString() shouldBe "{W}"
                }
                withClue("Aunt May should be a Legendary Creature — Human Citizen") {
                    cardDef?.typeLine?.toString() shouldBe "Legendary Creature — Human Citizen"
                }
                withClue("Aunt May should be 0/2") {
                    cardDef?.creatureStats?.power?.description shouldBe "0"
                    cardDef?.creatureStats?.toughness?.description shouldBe "2"
                }
            }
        }
    }
}
