package com.wingedsheep.gameserver.scenarios

import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.gameserver.ScenarioTestBase
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Angry Rabble.
 *
 * Card reference:
 * - Angry Rabble ({1}{R}): Creature — Human Citizen, 2/2
 *   "Trample"
 *   "Whenever a player casts a spell with mana value 4 or greater, Angry Rabble deals 1 damage to each opponent."
 *   "{5}{R}: Put two +1/+1 counters on Angry Rabble. Activate only as a sorcery."
 */
class AngryRabbleScenarioTest : ScenarioTestBase() {

    init {
        context("Angry Rabble trigger — cast-spell with mana value 4+") {

            test("deals 1 damage to opponent when a spell with mana value 4 or greater is cast") {
                val game = scenario()
                    .withPlayers("Caster", "Opponent")
                    .withCardOnBattlefield(1, "Angry Rabble")
                    .withCardInHand(1, "Fire Elemental")      // {3}{R}{R}, MV 5
                    .withLandsOnBattlefield(1, "Mountain", 5)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val castResult = game.castSpell(1, "Fire Elemental")
                withClue("Casting Fire Elemental should succeed: ${castResult.error}") {
                    castResult.error shouldBe null
                }
                game.resolveStack()

                withClue("Opponent should be at 19 life after Angry Rabble's trigger") {
                    game.getLifeTotal(2) shouldBe 19
                }
            }

            test("does not trigger when a spell with mana value 3 or less is cast") {
                val game = scenario()
                    .withPlayers("Caster", "Opponent")
                    .withCardOnBattlefield(1, "Angry Rabble")
                    .withCardInHand(1, "Glory Seeker")         // {1}{W}, MV 2
                    .withLandsOnBattlefield(1, "Plains", 2)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val castResult = game.castSpell(1, "Glory Seeker")
                withClue("Casting Glory Seeker should succeed: ${castResult.error}") {
                    castResult.error shouldBe null
                }
                game.resolveStack()

                withClue("Opponent should remain at 20 life (trigger did not fire for MV <= 3)") {
                    game.getLifeTotal(2) shouldBe 20
                }
            }
        }

        context("Angry Rabble — casting") {

            test("enters the battlefield as a 2/2 Trample Human Citizen when cast for {1}{R}") {
                val game = scenario()
                    .withPlayers("Caster", "Opponent")
                    .withCardInHand(1, "Angry Rabble")
                    .withLandsOnBattlefield(1, "Mountain", 2)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val castResult = game.castSpell(1, "Angry Rabble")
                withClue("Casting Angry Rabble for {1}{R} should succeed: ${castResult.error}") {
                    castResult.error shouldBe null
                }
                game.resolveStack()

                withClue("Angry Rabble should be on the battlefield") {
                    game.isOnBattlefield("Angry Rabble") shouldBe true
                }
                withClue("Angry Rabble should no longer be in the caster's hand") {
                    game.isInHand(1, "Angry Rabble") shouldBe false
                }

                val permanentId = game.findPermanent("Angry Rabble")!!
                val card = game.state.getEntity(permanentId)?.get<CardComponent>()!!

                withClue("Angry Rabble type line should be 'Creature — Human Citizen'") {
                    card.typeLine.toString() shouldBe "Creature — Human Citizen"
                }
                withClue("Angry Rabble should have Trample") {
                    card.baseKeywords shouldContain Keyword.TRAMPLE
                }
                withClue("Angry Rabble should be a 2/2") {
                    card.baseStats?.basePower shouldBe 2
                    card.baseStats?.baseToughness shouldBe 2
                }
            }
        }
    }
}
