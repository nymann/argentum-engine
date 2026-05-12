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

        context("Selfless Police Captain — leaves-the-battlefield trigger moves counters") {

            test("moves its +1/+1 counter onto a target creature you control when it leaves the battlefield") {
                // Accursed Centaur ETB forces a sacrifice — we sacrifice the captain so it leaves
                // and its +1/+1 counter should transfer to Glory Seeker.
                val game = scenario()
                    .withPlayers("Caster", "Opponent")
                    .withCardInHand(1, "Selfless Police Captain")
                    .withCardInHand(1, "Accursed Centaur")
                    .withCardOnBattlefield(1, "Glory Seeker")
                    .withLandsOnBattlefield(1, "Plains", 2)
                    .withLandsOnBattlefield(1, "Swamp", 1)
                    .withCardInLibrary(1, "Plains")
                    .withCardInLibrary(2, "Plains")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                // Cast captain — replacement effect gives it one +1/+1 counter
                val castCaptainResult = game.castSpell(1, "Selfless Police Captain")
                withClue("Casting Selfless Police Captain should succeed: ${castCaptainResult.error}") {
                    castCaptainResult.error shouldBe null
                }
                game.resolveStack()

                withClue("Selfless Police Captain should be on the battlefield") {
                    game.isOnBattlefield("Selfless Police Captain") shouldBe true
                }

                // Cast Accursed Centaur — ETB trigger requires sacrificing a creature
                val castCentaurResult = game.castSpell(1, "Accursed Centaur")
                withClue("Casting Accursed Centaur should succeed: ${castCentaurResult.error}") {
                    castCentaurResult.error shouldBe null
                }
                game.resolveStack()

                // Sacrifice Selfless Police Captain to satisfy Accursed Centaur's ETB
                val captainId = game.findPermanent("Selfless Police Captain")!!
                game.selectCards(listOf(captainId))

                // LTB trigger fires — select Glory Seeker as the counter recipient
                val glorySeekerID = game.findPermanent("Glory Seeker")!!
                game.selectTargets(listOf(glorySeekerID))

                game.resolveStack()

                withClue("Selfless Police Captain should no longer be on the battlefield") {
                    game.isOnBattlefield("Selfless Police Captain") shouldBe false
                }

                val counters = game.state.getEntity(glorySeekerID)?.get<CountersComponent>()
                withClue("Glory Seeker should have received exactly one +1/+1 counter") {
                    counters shouldNotBe null
                    counters!!.getCount(CounterType.PLUS_ONE_PLUS_ONE) shouldBe 1
                }

                val projected = game.state.projectedState
                withClue("Glory Seeker's effective power should be 3 (base 2 + 1 from counter)") {
                    projected.getPower(glorySeekerID) shouldBe 3
                }
                withClue("Glory Seeker's effective toughness should be 3 (base 2 + 1 from counter)") {
                    projected.getToughness(glorySeekerID) shouldBe 3
                }
            }
        }
    }
}
