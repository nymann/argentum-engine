package com.wingedsheep.gameserver.scenarios

import com.wingedsheep.gameserver.ScenarioTestBase
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Secret Identity.
 *
 * Card reference:
 * - Secret Identity ({U}): Instant
 *   Choose one —
 *   • Reveal — Until end of turn, target creature becomes a Hero with base power and toughness 3/4
 *     and gains flying and vigilance.
 *   • Conceal — Until end of turn, target creature becomes a Citizen with base power and toughness 1/1
 *     and gains hexproof.
 */
class SecretIdentityScenarioTest : ScenarioTestBase() {

    init {
        context("Secret Identity — Reveal mode") {

            test("Reveal transforms target 2/2 creature into Hero 3/4 with flying and vigilance until end of turn, then reverts") {
                val game = scenario()
                    .withPlayers("Caster", "Opponent")
                    .withCardInHand(1, "Secret Identity")
                    .withCardOnBattlefield(1, "Glory Seeker")   // 2/2 Human Soldier, no special abilities
                    .withLandsOnBattlefield(1, "Island", 1)
                    .withCardInLibrary(1, "Island")
                    .withCardInLibrary(2, "Island")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val creatureId = game.findPermanent("Glory Seeker")!!

                // Cast Secret Identity, Reveal mode (mode index 0), targeting the 2/2 Glory Seeker
                val castResult = game.castSpellWithMode(1, "Secret Identity", 0, creatureId)
                withClue("Casting Secret Identity (Reveal) should succeed: ${castResult.error}") {
                    castResult.error shouldBe null
                }
                game.resolveStack()

                withClue("Secret Identity should be in the graveyard after resolution") {
                    game.isInGraveyard(1, "Secret Identity") shouldBe true
                }

                val projectedAfterReveal = game.state.projectedState

                withClue("Creature type line should include Hero subtype") {
                    projectedAfterReveal.hasSubtype(creatureId, "Hero") shouldBe true
                }
                withClue("Creature type line should no longer include Human subtype") {
                    projectedAfterReveal.hasSubtype(creatureId, "Human") shouldBe false
                }
                withClue("Creature type line should no longer include Soldier subtype") {
                    projectedAfterReveal.hasSubtype(creatureId, "Soldier") shouldBe false
                }
                withClue("Creature base power should be 3") {
                    projectedAfterReveal.getPower(creatureId) shouldBe 3
                }
                withClue("Creature base toughness should be 4") {
                    projectedAfterReveal.getToughness(creatureId) shouldBe 4
                }
                withClue("Creature should have flying") {
                    projectedAfterReveal.hasKeyword(creatureId, Keyword.FLYING) shouldBe true
                }
                withClue("Creature should have vigilance") {
                    projectedAfterReveal.hasKeyword(creatureId, Keyword.VIGILANCE) shouldBe true
                }

                // Advance through the current turn's cleanup step
                game.passUntilPhase(Phase.ENDING, Step.CLEANUP)
                game.resolveStack()

                val projectedAfterCleanup = game.state.projectedState

                withClue("Creature should revert to power 2 after end of turn") {
                    projectedAfterCleanup.getPower(creatureId) shouldBe 2
                }
                withClue("Creature should revert to toughness 2 after end of turn") {
                    projectedAfterCleanup.getToughness(creatureId) shouldBe 2
                }
                withClue("Creature should revert to Human subtype after end of turn") {
                    projectedAfterCleanup.hasSubtype(creatureId, "Human") shouldBe true
                }
                withClue("Creature should revert to Soldier subtype after end of turn") {
                    projectedAfterCleanup.hasSubtype(creatureId, "Soldier") shouldBe true
                }
            }
        }

        context("Secret Identity — Conceal mode") {

            test("Conceal transforms target 2/2 creature into Citizen 1/1 with hexproof until end of turn, then reverts") {
                val game = scenario()
                    .withPlayers("Caster", "Opponent")
                    .withCardInHand(1, "Secret Identity")
                    .withCardOnBattlefield(1, "Glory Seeker")   // 2/2 Human Soldier, no special abilities
                    .withLandsOnBattlefield(1, "Island", 1)
                    .withCardInLibrary(1, "Island")
                    .withCardInLibrary(2, "Island")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val creatureId = game.findPermanent("Glory Seeker")!!

                // Cast Secret Identity, Conceal mode (mode index 1), targeting the 2/2 Glory Seeker
                val castResult = game.castSpellWithMode(1, "Secret Identity", 1, creatureId)
                withClue("Casting Secret Identity (Conceal) should succeed: ${castResult.error}") {
                    castResult.error shouldBe null
                }
                game.resolveStack()

                withClue("Secret Identity should be in the graveyard after resolution") {
                    game.isInGraveyard(1, "Secret Identity") shouldBe true
                }

                val projectedAfterConceal = game.state.projectedState

                withClue("Creature type line should include Citizen subtype") {
                    projectedAfterConceal.hasSubtype(creatureId, "Citizen") shouldBe true
                }
                withClue("Creature type line should no longer include Human subtype") {
                    projectedAfterConceal.hasSubtype(creatureId, "Human") shouldBe false
                }
                withClue("Creature type line should no longer include Soldier subtype") {
                    projectedAfterConceal.hasSubtype(creatureId, "Soldier") shouldBe false
                }
                withClue("Creature base power should be 1") {
                    projectedAfterConceal.getPower(creatureId) shouldBe 1
                }
                withClue("Creature base toughness should be 1") {
                    projectedAfterConceal.getToughness(creatureId) shouldBe 1
                }
                withClue("Creature should have hexproof") {
                    projectedAfterConceal.hasKeyword(creatureId, Keyword.HEXPROOF) shouldBe true
                }

                // Advance through the current turn's cleanup step
                game.passUntilPhase(Phase.ENDING, Step.CLEANUP)
                game.resolveStack()

                val projectedAfterCleanup = game.state.projectedState

                withClue("Creature should revert to power 2 after end of turn") {
                    projectedAfterCleanup.getPower(creatureId) shouldBe 2
                }
                withClue("Creature should revert to toughness 2 after end of turn") {
                    projectedAfterCleanup.getToughness(creatureId) shouldBe 2
                }
                withClue("Creature should revert to Human subtype after end of turn") {
                    projectedAfterCleanup.hasSubtype(creatureId, "Human") shouldBe true
                }
                withClue("Creature should revert to Soldier subtype after end of turn") {
                    projectedAfterCleanup.hasSubtype(creatureId, "Soldier") shouldBe true
                }
            }
        }
    }
}
