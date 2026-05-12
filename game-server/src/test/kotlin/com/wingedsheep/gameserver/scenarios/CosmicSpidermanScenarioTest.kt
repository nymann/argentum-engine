package com.wingedsheep.gameserver.scenarios

import com.wingedsheep.engine.mechanics.layers.StateProjector
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.gameserver.ScenarioTestBase
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.core.Subtype
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Cosmic Spider-Man.
 *
 * Card reference:
 * - Cosmic Spider-Man ({W}{U}{B}{R}{G}): Legendary Creature — Spider Human Hero, 5/5
 *   Flying, first strike, trample, lifelink, haste
 *   At the beginning of combat: other Spiders you control gain flying, first strike,
 *   trample, lifelink, and haste until end of turn.
 */
class CosmicSpidermanScenarioTest : ScenarioTestBase() {

    private val stateProjector = StateProjector()

    init {
        context("Cosmic Spider-Man beginning-of-combat trigger") {

            test("grants five keywords to other friendly Spiders only, not opponent's Spider") {
                val game = scenario()
                    .withPlayers("Active", "Opponent")
                    .withCardOnBattlefield(1, "Cosmic Spider-Man")
                    .withCardOnBattlefield(1, "Skyward Spider")   // friendly Spider — gains keywords
                    .withCardOnBattlefield(2, "Skyward Spider")   // opponent's Spider — unaffected
                    .withCardInLibrary(1, "Island")
                    .withCardInLibrary(2, "Island")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                game.passUntilPhase(Phase.COMBAT, Step.BEGIN_COMBAT)
                game.resolveStack()

                val friendlySpiderId = game.state.getBattlefield(game.player1Id).find { entityId ->
                    game.state.getEntity(entityId)?.get<CardComponent>()?.name == "Skyward Spider"
                }!!
                val opponentSpiderId = game.state.getBattlefield(game.player2Id).find { entityId ->
                    game.state.getEntity(entityId)?.get<CardComponent>()?.name == "Skyward Spider"
                }!!

                val projected = stateProjector.project(game.state)

                withClue("Friendly Spider should gain flying from trigger") {
                    projected.hasKeyword(friendlySpiderId, Keyword.FLYING) shouldBe true
                }
                withClue("Friendly Spider should gain first strike from trigger") {
                    projected.hasKeyword(friendlySpiderId, Keyword.FIRST_STRIKE) shouldBe true
                }
                withClue("Friendly Spider should gain trample from trigger") {
                    projected.hasKeyword(friendlySpiderId, Keyword.TRAMPLE) shouldBe true
                }
                withClue("Friendly Spider should gain lifelink from trigger") {
                    projected.hasKeyword(friendlySpiderId, Keyword.LIFELINK) shouldBe true
                }
                withClue("Friendly Spider should gain haste from trigger") {
                    projected.hasKeyword(friendlySpiderId, Keyword.HASTE) shouldBe true
                }

                withClue("Opponent's Spider should not gain flying") {
                    projected.hasKeyword(opponentSpiderId, Keyword.FLYING) shouldBe false
                }
                withClue("Opponent's Spider should not gain first strike") {
                    projected.hasKeyword(opponentSpiderId, Keyword.FIRST_STRIKE) shouldBe false
                }
                withClue("Opponent's Spider should not gain trample") {
                    projected.hasKeyword(opponentSpiderId, Keyword.TRAMPLE) shouldBe false
                }
                withClue("Opponent's Spider should not gain lifelink") {
                    projected.hasKeyword(opponentSpiderId, Keyword.LIFELINK) shouldBe false
                }
                withClue("Opponent's Spider should not gain haste") {
                    projected.hasKeyword(opponentSpiderId, Keyword.HASTE) shouldBe false
                }
            }
        }

        context("Cosmic Spider-Man cast and resolve") {

            test("resolves onto battlefield as 5/5 Legendary Creature — Spider Human Hero with five static keywords") {
                val game = scenario()
                    .withPlayers("Caster", "Opponent")
                    .withCardInHand(1, "Cosmic Spider-Man")
                    .withLandsOnBattlefield(1, "Plains", 1)
                    .withLandsOnBattlefield(1, "Island", 1)
                    .withLandsOnBattlefield(1, "Swamp", 1)
                    .withLandsOnBattlefield(1, "Mountain", 1)
                    .withLandsOnBattlefield(1, "Forest", 1)
                    .withCardInLibrary(1, "Island")
                    .withCardInLibrary(2, "Island")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val castResult = game.castSpell(1, "Cosmic Spider-Man")
                withClue("Casting Cosmic Spider-Man should succeed: ${castResult.error}") {
                    castResult.error shouldBe null
                }

                game.resolveStack()

                withClue("Cosmic Spider-Man should be on the battlefield") {
                    game.isOnBattlefield("Cosmic Spider-Man") shouldBe true
                }

                val cosmicId = game.findPermanent("Cosmic Spider-Man")!!
                val cardComponent = game.state.getEntity(cosmicId)!!.get<CardComponent>()!!

                withClue("Should be a 5/5") {
                    stateProjector.getProjectedPower(game.state, cosmicId) shouldBe 5
                    stateProjector.getProjectedToughness(game.state, cosmicId) shouldBe 5
                }

                withClue("Should be Legendary") {
                    cardComponent.typeLine.isLegendary shouldBe true
                }

                withClue("Should be a Creature") {
                    cardComponent.typeLine.isCreature shouldBe true
                }

                withClue("Type line should include Spider, Human, and Hero subtypes") {
                    cardComponent.typeLine.subtypes.containsAll(
                        setOf(Subtype.SPIDER, Subtype.HUMAN, Subtype("Hero"))
                    ) shouldBe true
                }

                val projected = stateProjector.project(game.state)

                withClue("Should have flying") {
                    projected.hasKeyword(cosmicId, Keyword.FLYING) shouldBe true
                }
                withClue("Should have first strike") {
                    projected.hasKeyword(cosmicId, Keyword.FIRST_STRIKE) shouldBe true
                }
                withClue("Should have trample") {
                    projected.hasKeyword(cosmicId, Keyword.TRAMPLE) shouldBe true
                }
                withClue("Should have lifelink") {
                    projected.hasKeyword(cosmicId, Keyword.LIFELINK) shouldBe true
                }
                withClue("Should have haste") {
                    projected.hasKeyword(cosmicId, Keyword.HASTE) shouldBe true
                }
            }
        }
    }
}
