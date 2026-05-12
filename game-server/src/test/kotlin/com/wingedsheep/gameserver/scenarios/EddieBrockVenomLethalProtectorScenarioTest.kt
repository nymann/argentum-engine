package com.wingedsheep.gameserver.scenarios

import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.state.components.identity.DoubleFacedComponent
import com.wingedsheep.gameserver.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Scenario tests for Eddie Brock // Venom, Lethal Protector.
 *
 * Card reference:
 * - Front face: Eddie Brock ({2}{B}): Legendary Creature — Human Hero Villain
 * - Back face: Venom, Lethal Protector: Legendary Creature — Symbiote (Haste, Trample, Menace)
 */
class EddieBrockVenomLethalProtectorScenarioTest : ScenarioTestBase() {

    private val cardName = "Eddie Brock // Venom, Lethal Protector"

    init {
        context("Eddie Brock // Venom, Lethal Protector — card registration") {

            test("is registered in SpmSet and resolvable by full double-faced name") {
                val cardDef = cardRegistry.getCard(cardName)
                withClue("Card '$cardName' must be registered in SpmSet") {
                    cardDef shouldNotBe null
                }
            }
        }

        context("Eddie Brock // Venom, Lethal Protector — cast front face") {

            test("front face enters the battlefield with correct type line and is not transformed") {
                val game = scenario()
                    .withPlayers("Caster", "Opponent")
                    .withCardInHand(1, cardName)
                    .withLandsOnBattlefield(1, "Swamp", 5)
                    .withCardInLibrary(1, "Swamp")
                    .withCardInLibrary(2, "Swamp")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val castResult = game.castSpell(1, cardName)
                withClue("Casting $cardName should succeed: ${castResult.error}") {
                    castResult.error shouldBe null
                }
                game.resolveStack()

                withClue("$cardName should be on the battlefield") {
                    game.isOnBattlefield(cardName) shouldBe true
                }

                val entityId = game.findPermanent(cardName)!!
                val cardComponent = game.state.getEntity(entityId)?.get<CardComponent>()

                withClue("Front face type line should be 'Legendary Creature — Human Hero Villain'") {
                    cardComponent?.typeLine shouldBe "Legendary Creature — Human Hero Villain"
                }

                withClue("Permanent should be on front face (not transformed)") {
                    val dfcComp = game.state.getEntity(entityId)?.get<DoubleFacedComponent>()
                    (dfcComp == null || dfcComp.currentFace == DoubleFacedComponent.Face.FRONT) shouldBe true
                }
            }
        }
    }
}
