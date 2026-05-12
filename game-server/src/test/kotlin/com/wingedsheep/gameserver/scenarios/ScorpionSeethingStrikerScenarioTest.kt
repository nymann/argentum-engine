package com.wingedsheep.gameserver.scenarios

import com.wingedsheep.engine.mechanics.layers.StateProjector
import com.wingedsheep.gameserver.ScenarioTestBase
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.Rarity
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Scorpion, Seething Striker.
 *
 * Card reference:
 * - Scorpion, Seething Striker ({3}{B}): Legendary Creature — Scorpion Human Villain, 3/3
 *   "Deathtouch"
 *   Uncommon, collector number 64, artist Simon Dominic
 */
class ScorpionSeethingStrikerScenarioTest : ScenarioTestBase() {

    private val stateProjector = StateProjector()

    init {
        context("Scorpion, Seething Striker — card definition") {

            test("cast with {3}{B} enters battlefield as a 3/3 Legendary Creature — Scorpion Human Villain with deathtouch") {
                // 1 Swamp supplies {B}, 3 Islands supply the 3 generic mana.
                val game = scenario()
                    .withPlayers("Active", "Opponent")
                    .withCardInHand(1, "Scorpion, Seething Striker")
                    .withLandsOnBattlefield(1, "Swamp", 1)
                    .withLandsOnBattlefield(1, "Island", 3)
                    .withCardInLibrary(1, "Island")
                    .withCardInLibrary(1, "Island")
                    .withCardInLibrary(2, "Island")
                    .withCardInLibrary(2, "Island")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val castResult = game.castSpell(1, "Scorpion, Seething Striker")
                withClue("Casting Scorpion, Seething Striker for {3}{B} should succeed: ${castResult.error}") {
                    castResult.error shouldBe null
                }
                game.resolveStack()

                withClue("Scorpion, Seething Striker should be on the battlefield") {
                    game.isOnBattlefield("Scorpion, Seething Striker") shouldBe true
                }

                val scorpionId = game.findPermanent("Scorpion, Seething Striker")!!
                val projected = stateProjector.project(game.state)

                withClue("Scorpion should be a 3/3") {
                    projected.getPower(scorpionId) shouldBe 3
                    projected.getToughness(scorpionId) shouldBe 3
                }

                withClue("Scorpion should have deathtouch") {
                    projected.hasKeyword(scorpionId, Keyword.DEATHTOUCH) shouldBe true
                }

                val clientState = game.getClientState(1)
                val card = clientState.cards[scorpionId]
                card.shouldNotBeNull()

                withClue("Scorpion should be a Legendary Creature") {
                    card.cardTypes shouldContain "CREATURE"
                    card.typeLine.contains("Legendary") shouldBe true
                }

                withClue("Scorpion should have correct subtypes") {
                    card.subtypes shouldContain "Scorpion"
                    card.subtypes shouldContain "Human"
                    card.subtypes shouldContain "Villain"
                }
            }
        }

        context("Scorpion, Seething Striker — metadata matches Scryfall") {

            test("card definition has correct rarity, collector number, artist, oracle text, and image URI") {
                val cardDef = cardRegistry.getCard("Scorpion, Seething Striker")
                cardDef.shouldNotBeNull()

                withClue("rarity should be UNCOMMON") {
                    cardDef.metadata.rarity shouldBe Rarity.UNCOMMON
                }
                withClue("collector number should be 64") {
                    cardDef.metadata.collectorNumber shouldBe "64"
                }
                withClue("artist should be Simon Dominic") {
                    cardDef.metadata.artist shouldBe "Simon Dominic"
                }
                withClue("oracle text should list Deathtouch") {
                    cardDef.oracleText.contains("Deathtouch") shouldBe true
                }
                withClue("image URI should be from Scryfall") {
                    cardDef.metadata.imageUri.shouldNotBeNull()
                    cardDef.metadata.imageUri!!.startsWith("https://cards.scryfall.io/") shouldBe true
                }
            }
        }
    }
}
