package com.wingedsheep.gameserver.scenarios

import com.wingedsheep.engine.state.components.battlefield.AttachedToComponent
import com.wingedsheep.engine.state.components.battlefield.AttachmentsComponent
import com.wingedsheep.engine.state.components.battlefield.CountersComponent
import com.wingedsheep.gameserver.ScenarioTestBase
import com.wingedsheep.sdk.core.CounterType
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Scenario tests for Biorganic Carapace.
 *
 * Card reference:
 * - Biorganic Carapace ({2}{W}{U}): Artifact — Equipment
 *   "When this Equipment enters, attach it to target creature you control."
 *   "Equipped creature gets +2/+2."
 *   "Equip {2}"
 */
class BiorganicCarapaceScenarioTest : ScenarioTestBase() {

    init {
        context("Biorganic Carapace combat damage trigger") {

            test("draws one card per modified creature you control when equipped creature deals combat damage") {
                // Glory Seeker (2/2) equipped → 4/4 (modified by equipment)
                // Devoted Hero (1/2) with a +1/+1 counter → also modified
                // Enormous Baloth — unmodified, must NOT be counted
                val game = scenario()
                    .withPlayers("Caster", "Opponent")
                    .withCardOnBattlefield(1, "Biorganic Carapace")
                    .withCardOnBattlefield(1, "Glory Seeker")
                    .withCardOnBattlefield(1, "Devoted Hero")
                    .withCardOnBattlefield(1, "Enormous Baloth")
                    .withActivePlayer(1)
                    .inPhase(Phase.COMBAT, Step.DECLARE_ATTACKERS)
                    .build()

                val carapaceId = game.findPermanent("Biorganic Carapace")!!
                val glorySeekerId = game.findPermanent("Glory Seeker")!!
                val devotedHeroId = game.findPermanent("Devoted Hero")!!

                // Pre-attach Biorganic Carapace to Glory Seeker
                game.state = game.state
                    .updateEntity(carapaceId) { it.with(AttachedToComponent(glorySeekerId)) }
                    .updateEntity(glorySeekerId) { container ->
                        val existing = container.get<AttachmentsComponent>()
                        container.with(AttachmentsComponent((existing?.attachedIds ?: emptyList()) + carapaceId))
                    }

                // Give Devoted Hero a +1/+1 counter so it is also modified
                game.state = game.state.updateEntity(devotedHeroId) { container ->
                    container.with(CountersComponent().withAdded(CounterType.PLUS_ONE_PLUS_ONE, 1))
                }

                val initialHandSize = game.handSize(1)

                // Attack with the equipped 4/4 Glory Seeker
                val attackResult = game.declareAttackers(mapOf("Glory Seeker" to 2))
                withClue("Declaring attacker should succeed: ${attackResult.error}") {
                    attackResult.error shouldBe null
                }

                game.passUntilPhase(Phase.COMBAT, Step.DECLARE_BLOCKERS)
                game.declareNoBlockers()

                // Advance through combat damage — the granted trigger fires, draws 2 cards
                game.passUntilPhase(Phase.POSTCOMBAT_MAIN, Step.POSTCOMBAT_MAIN)

                withClue("Defending player should have taken 4 combat damage from the equipped 4/4") {
                    game.getLifeTotal(2) shouldBe 16
                }
                withClue("Active player draws 2 cards: one for equipped Glory Seeker, one for counter-bearing Devoted Hero; Enormous Baloth is unmodified and not counted") {
                    game.handSize(1) shouldBe initialHandSize + 2
                }
            }
        }

        context("Biorganic Carapace ETB auto-attach") {

            test("entering the battlefield attaches to chosen creature and grants +2/+2") {
                val game = scenario()
                    .withPlayers("Caster", "Opponent")
                    .withCardInHand(1, "Biorganic Carapace")
                    .withCardOnBattlefield(1, "Glory Seeker") // vanilla 2/2
                    .withLandsOnBattlefield(1, "Plains", 2)
                    .withLandsOnBattlefield(1, "Island", 2)
                    .withCardInLibrary(1, "Plains")
                    .withCardInLibrary(2, "Island")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val glorySeekerId = game.findPermanent("Glory Seeker")!!

                withClue("Glory Seeker base power should be 2") {
                    game.state.projectedState.getPower(glorySeekerId) shouldBe 2
                }
                withClue("Glory Seeker base toughness should be 2") {
                    game.state.projectedState.getToughness(glorySeekerId) shouldBe 2
                }

                val castResult = game.castSpell(1, "Biorganic Carapace")
                withClue("Casting Biorganic Carapace should succeed: ${castResult.error}") {
                    castResult.error shouldBe null
                }

                // Resolve the spell; the ETB trigger fires and waits for target selection
                game.resolveStack()

                withClue("ETB trigger should present a target decision") {
                    game.hasPendingDecision() shouldBe true
                }

                game.selectTargets(listOf(glorySeekerId))
                game.resolveStack()

                withClue("Biorganic Carapace should be on the battlefield") {
                    game.isOnBattlefield("Biorganic Carapace") shouldBe true
                }

                val carapaceId = game.findPermanent("Biorganic Carapace")!!
                val attachedTo = game.state.getEntity(carapaceId)!!.get<AttachedToComponent>()
                withClue("Biorganic Carapace should be attached to Glory Seeker") {
                    attachedTo shouldNotBe null
                    attachedTo!!.targetId shouldBe glorySeekerId
                }

                withClue("Equipped Glory Seeker power should be 4 (2 + 2)") {
                    game.state.projectedState.getPower(glorySeekerId) shouldBe 4
                }
                withClue("Equipped Glory Seeker toughness should be 4 (2 + 2)") {
                    game.state.projectedState.getToughness(glorySeekerId) shouldBe 4
                }

                withClue("Biorganic Carapace should no longer be in hand") {
                    game.isInHand(1, "Biorganic Carapace") shouldBe false
                }
            }
        }
    }
}
