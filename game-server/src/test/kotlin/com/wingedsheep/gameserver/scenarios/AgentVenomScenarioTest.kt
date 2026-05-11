package com.wingedsheep.gameserver.scenarios

import com.wingedsheep.gameserver.ScenarioTestBase
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Scenario test for Agent Venom.
 *
 * Card reference:
 * - Agent Venom ({2}{B}): 2/3 Legendary Creature — Symbiote Soldier Hero
 *   Flash, Menace
 */
class AgentVenomScenarioTest : ScenarioTestBase() {

    init {
        context("Agent Venom — Flash allows casting at instant speed") {
            test("resolves during opponent's end step and enters as 2/3 Legendary Creature with Menace and Flash") {
                // Opponent (player 2) is the active player; it is their end step.
                // Player 1 has priority and 3 untapped Swamps ({B}{B}{B} satisfies {2}{B}).
                // Flash is the only reason this cast is legal outside a main phase.
                val game = scenario()
                    .withPlayers("Player", "Opponent")
                    .withCardInHand(1, "Agent Venom")
                    .withLandsOnBattlefield(1, "Swamp", 3)
                    .withCardInLibrary(1, "Swamp")
                    .withCardInLibrary(2, "Swamp")
                    .withActivePlayer(2)
                    .withPriorityPlayer(1)
                    .inPhase(Phase.ENDING, Step.END)
                    .build()

                game.castSpell(1, "Agent Venom")
                game.resolveStack()

                game.isOnBattlefield("Agent Venom") shouldBe true

                val agentVenomId = game.findPermanent("Agent Venom")
                agentVenomId shouldNotBe null

                val clientState = game.getClientState(1)
                val card = clientState.cards[agentVenomId!!]
                card shouldNotBe null

                card!!.power shouldBe 2
                card.toughness shouldBe 3

                card.cardTypes shouldContain "CREATURE"
                card.typeLine.contains("Legendary") shouldBe true

                card.subtypes shouldContain "Symbiote"
                card.subtypes shouldContain "Soldier"
                card.subtypes shouldContain "Hero"

                card.keywords shouldContain Keyword.MENACE
                card.keywords shouldContain Keyword.FLASH
            }
        }
    }
}
