package com.wingedsheep.gameserver.scenarios

import com.wingedsheep.engine.state.components.battlefield.CountersComponent
import com.wingedsheep.gameserver.ScenarioTestBase
import com.wingedsheep.sdk.core.CounterType
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Scenario tests for Selfless Police Captain.
 *
 * Card reference:
 * - Selfless Police Captain ({1}{W}): Creature — Human Soldier, 1/1
 *   "Selfless Police Captain enters with a +1/+1 counter on it."
 *   "When Selfless Police Captain leaves the battlefield, move its +1/+1 counters
 *    onto target creature you control."
 */
class SelflessPoliceCaptainScenarioTest : ScenarioTestBase() {

    init {
        context("Selfless Police Captain — enters with a +1/+1 counter") {

            test("enters the battlefield with one +1/+1 counter after being cast") {
                val game = scenario()
                    .withPlayers("Caster", "Opponent")
                    .withCardInHand(1, "Selfless Police Captain")
                    .withLandsOnBattlefield(1, "Plains", 2)
                    .withCardInLibrary(1, "Plains")
                    .withCardInLibrary(2, "Plains")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val castResult = game.castSpell(1, "Selfless Police Captain")
                withClue("Casting Selfless Police Captain should succeed: ${castResult.error}") {
                    castResult.error shouldBe null
                }
                game.resolveStack()

                withClue("Selfless Police Captain should be on the battlefield") {
                    game.isOnBattlefield("Selfless Police Captain") shouldBe true
                }
                withClue("Selfless Police Captain should no longer be in the active player's hand") {
                    game.isInHand(1, "Selfless Police Captain") shouldBe false
                }

                val captainId = game.findPermanent("Selfless Police Captain")!!
                val counters = game.state.getEntity(captainId)?.get<CountersComponent>()
                withClue("Selfless Police Captain should have exactly one +1/+1 counter") {
                    counters shouldNotBe null
                    counters!!.getCount(CounterType.PLUS_ONE_PLUS_ONE) shouldBe 1
                }

                val projected = game.state.projectedState
                withClue("Selfless Police Captain should have effective power of 2 (base 1 + counter)") {
                    projected.getPower(captainId) shouldBe 2
                }
                withClue("Selfless Police Captain should have effective toughness of 2 (base 1 + counter)") {
                    projected.getToughness(captainId) shouldBe 2
                }
            }
        }
    }
}
