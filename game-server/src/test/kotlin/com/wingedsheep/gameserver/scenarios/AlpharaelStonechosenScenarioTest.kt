package com.wingedsheep.gameserver.scenarios

import com.wingedsheep.gameserver.ScenarioTestBase
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

class AlpharaelStonechosenScenarioTest : ScenarioTestBase() {

    init {
        context("Alpharael, Stonechosen — cast for mana cost") {

            test("resolves as a 3/3 Legendary Human Cleric with Ward—Discard and Void attack trigger") {
                val game = scenario()
                    .withPlayers("ActivePlayer", "Opponent")
                    .withCardInHand(1, "Alpharael, Stonechosen")
                    .withLandsOnBattlefield(1, "Swamp", 5) // {3}{B}{B}
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                game.castSpell(1, "Alpharael, Stonechosen")
                game.resolveStack()

                withClue("Alpharael, Stonechosen should be on the battlefield") {
                    game.isOnBattlefield("Alpharael, Stonechosen") shouldBe true
                }

                val alpharaelId = game.findPermanent("Alpharael, Stonechosen")!!
                val projected = game.state.projectedState

                withClue("Should be a 3/3") {
                    projected.getPower(alpharaelId) shouldBe 3
                    projected.getToughness(alpharaelId) shouldBe 3
                }

                withClue("Should be Legendary") {
                    projected.isLegendary(alpharaelId) shouldBe true
                }

                withClue("Should be a Human Cleric") {
                    projected.hasSubtype(alpharaelId, "Human") shouldBe true
                    projected.hasSubtype(alpharaelId, "Cleric") shouldBe true
                }

                withClue("Should have Ward—Discard a card at random triggered ability") {
                    projected.hasKeyword(alpharaelId, Keyword.WARD) shouldBe true
                }

                // Void attack trigger verified via S3-S5 scenarios below
            }
        }

        context("Alpharael, Stonechosen — Void attack trigger") {

            test("defending player loses half their life rounded up when Alpharael attacks and a nonland permanent left the battlefield this turn") {
                val game = scenario()
                    .withPlayers("ActivePlayer", "Opponent")
                    .withCardOnBattlefield(1, "Alpharael, Stonechosen")
                    .withCardOnBattlefield(2, "Devoted Hero") // 1/2 — Shock fodder to satisfy Void
                    .withLandsOnBattlefield(1, "Mountain", 1)
                    .withCardInHand(1, "Shock")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                // Meet the Void condition: kill a nonland permanent this turn
                val devotedHeroId = game.findPermanent("Devoted Hero")!!
                game.castSpell(1, "Shock", devotedHeroId)
                game.resolveStack()

                val opponentLifeBefore = game.getLifeTotal(2)

                game.passUntilPhase(Phase.COMBAT, Step.DECLARE_ATTACKERS)
                game.declareAttackers(mapOf("Alpharael, Stonechosen" to 2))
                game.resolveStack()

                withClue("Defending player should lose half their life rounded up") {
                    val expectedLoss = (opponentLifeBefore + 1) / 2
                    game.getLifeTotal(2) shouldBe opponentLifeBefore - expectedLoss
                }
            }
        }
    }
}
