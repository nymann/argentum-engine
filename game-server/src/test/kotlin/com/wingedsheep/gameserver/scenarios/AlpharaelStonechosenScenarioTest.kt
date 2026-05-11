package com.wingedsheep.gameserver.scenarios

import com.wingedsheep.engine.core.ActivateAbility
import com.wingedsheep.engine.state.components.stack.ChosenTarget
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

        context("Alpharael, Stonechosen — Ward—Discard a card at random") {

            test("ward counters opponent's targeting spell when opponent has no cards to discard") {
                val game = scenario()
                    .withPlayers("PlayerA", "PlayerB")
                    .withCardOnBattlefield(1, "Alpharael, Stonechosen")
                    .withCardInHand(2, "Bring Low") // {3}{R} — deals 3 to target creature
                    .withLandsOnBattlefield(2, "Mountain", 4)
                    .withActivePlayer(2)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val alpharaelId = game.findPermanent("Alpharael, Stonechosen")!!

                // P2 casts Bring Low targeting Alpharael — P2 has no other cards, cannot pay ward cost
                game.castSpell(2, "Bring Low", alpharaelId)
                game.resolveStack()

                // Ward triggered: P2 had no cards to discard at random → ward cost unpayable
                // → Bring Low must be countered → Alpharael survives
                withClue("Alpharael should still be on the battlefield (Bring Low countered by ward)") {
                    game.isOnBattlefield("Alpharael, Stonechosen") shouldBe true
                }
            }

            test("ward — opponent discards a card at random and targeting spell resolves when opponent can pay") {
                val game = scenario()
                    .withPlayers("PlayerA", "PlayerB")
                    .withCardOnBattlefield(1, "Alpharael, Stonechosen")
                    .withCardInHand(2, "Bring Low") // {3}{R}
                    .withCardInHand(2, "Devoted Hero") // ward payment fodder (discarded at random)
                    .withLandsOnBattlefield(2, "Mountain", 4)
                    .withActivePlayer(2)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val alpharaelId = game.findPermanent("Alpharael, Stonechosen")!!

                // P2 casts Bring Low targeting Alpharael; P2 has 1 other card as ward payment
                game.castSpell(2, "Bring Low", alpharaelId)
                game.resolveStack()

                // Ward triggered: P2 discarded Devoted Hero at random as ward payment
                // P2's hand is now empty (cast Bring Low, discarded Devoted Hero)
                withClue("Player B should have discarded 1 card at random as ward payment") {
                    game.handSize(2) shouldBe 0
                }

                // Bring Low resolved after ward was paid → Alpharael (3/3) took 3 damage and died
                withClue("Alpharael should be in the graveyard (Bring Low resolved after ward paid)") {
                    game.isInGraveyard(1, "Alpharael, Stonechosen") shouldBe true
                }
            }
        }

        context("Alpharael, Stonechosen — Ward triggers from opponent's activated ability") {

            test("ward — opponent discards a card at random when their activated ability targets Alpharael") {
                val game = scenario()
                    .withPlayers("PlayerA", "PlayerB")
                    .withCardOnBattlefield(1, "Alpharael, Stonechosen")
                    .withCardOnBattlefield(2, "Embermage Goblin") // {T}: deal 1 to any target
                    .withCardInHand(2, "Devoted Hero") // ward payment fodder
                    .withActivePlayer(2)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val alpharaelId = game.findPermanent("Alpharael, Stonechosen")!!
                val embermageId = game.findPermanent("Embermage Goblin")!!

                val tapAbility = cardRegistry.getCard("Embermage Goblin")!!
                    .script.activatedAbilities[0]

                // P2 activates Embermage Goblin's tap ability targeting Alpharael
                // P2 has Devoted Hero in hand — should be discarded at random as ward payment
                val activateResult = game.execute(
                    ActivateAbility(
                        playerId = game.player2Id,
                        sourceId = embermageId,
                        abilityId = tapAbility.id,
                        targets = listOf(ChosenTarget.Permanent(alpharaelId))
                    )
                )
                withClue("Ability activation should succeed: ${activateResult.error}") {
                    activateResult.error shouldBe null
                }

                game.resolveStack()

                // Ward triggered: P2 discarded Devoted Hero at random as payment
                withClue("Player B should have discarded 1 card at random (ward payment for activated ability)") {
                    game.handSize(2) shouldBe 0
                }
            }
        }

        context("Alpharael, Stonechosen — Void attack trigger") {

            test("defending player at 7 life loses 4 (half of 7 rounded up) when Alpharael attacks and Void is met") {
                val game = scenario()
                    .withPlayers("ActivePlayer", "Opponent")
                    .withCardOnBattlefield(1, "Alpharael, Stonechosen")
                    .withCardOnBattlefield(2, "Devoted Hero")
                    .withLandsOnBattlefield(1, "Mountain", 1)
                    .withCardInHand(1, "Shock")
                    .withLifeTotal(2, 7)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val devotedHeroId = game.findPermanent("Devoted Hero")!!
                game.castSpell(1, "Shock", devotedHeroId)
                game.resolveStack()

                game.passUntilPhase(Phase.COMBAT, Step.DECLARE_ATTACKERS)
                game.declareAttackers(mapOf("Alpharael, Stonechosen" to 2))
                game.resolveStack()

                withClue("Defending player should lose 4 life (ceil(7/2)) and be at 3") {
                    game.getLifeTotal(2) shouldBe 3
                }
            }

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
