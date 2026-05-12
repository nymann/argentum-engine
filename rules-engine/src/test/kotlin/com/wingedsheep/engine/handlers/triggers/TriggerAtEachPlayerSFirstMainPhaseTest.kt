package com.wingedsheep.engine.handlers.triggers

import com.wingedsheep.engine.state.components.stack.TriggeredAbilityOnStackComponent
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.scripting.GameEvent.StepEvent
import com.wingedsheep.sdk.scripting.TriggerBinding
import com.wingedsheep.sdk.scripting.TriggerSpec
import com.wingedsheep.sdk.scripting.references.Player
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Verifies that a "at the beginning of each player's first main phase" trigger:
 *  - fires exactly once per precombat main phase (not postcombat),
 *  - uses the player whose main phase began as the ability's controller,
 *    regardless of who controls the source permanent.
 */
class TriggerAtEachPlayerSFirstMainPhaseTest : FunSpec({

    val eachFirstMainCard = card("Each First Main Trigger Card") {
        manaCost = "{2}{G}"
        typeLine = "Creature — Elf"
        power = 2
        toughness = 2
        oracleText = "At the beginning of each player's first main phase, that player draws a card."

        triggeredAbility {
            trigger = TriggerSpec(
                event = StepEvent(Step.PRECOMBAT_MAIN, Player.Each),
                binding = TriggerBinding.ANY
            )
            effect = Effects.DrawCards(1)
        }
    }

    fun createDriver(): GameTestDriver {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all + listOf(eachFirstMainCard))
        return driver
    }

    fun topTrigger(driver: GameTestDriver): TriggeredAbilityOnStackComponent {
        val topId = driver.getTopOfStack()
            ?: throw AssertionError("Stack is empty")
        return driver.state.getEntity(topId)
            ?.get<TriggeredAbilityOnStackComponent>()
            ?: throw AssertionError("Top of stack is not a triggered ability")
    }

    test("trigger fires for each player's first main phase with that player as ability controller, not the permanent's controller") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Forest" to 40))

        val playerA = driver.activePlayer!!
        val playerB = driver.getOpponent(playerA)

        // Place the permanent during playerA's precombat main so it is present for
        // subsequent main phases but does not trigger on this first visit (the step
        // already changed before the permanent arrived).
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)
        driver.putPermanentOnBattlefield(playerA, "Each First Main Trigger Card")

        // Postcombat (second) main phase must NOT fire the trigger.
        driver.passPriorityUntil(Step.POSTCOMBAT_MAIN, maxPasses = 200)
        driver.stackSize shouldBe 0

        // --- PlayerB's precombat main phase ---
        // The trigger fires from playerA's permanent; the ability controller must
        // be playerB (the player whose main phase began), not playerA (the owner).
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN, maxPasses = 200)
        driver.activePlayer shouldBe playerB
        driver.stackSize shouldBe 1
        topTrigger(driver).controllerId shouldBe playerB  // fails with current code (yields playerA)

        // Resolve playerB's trigger, then advance to playerA's next precombat main.
        driver.bothPass()
        driver.passPriorityUntil(Step.POSTCOMBAT_MAIN, maxPasses = 200)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN, maxPasses = 200)
        driver.activePlayer shouldBe playerA
        driver.stackSize shouldBe 1
        topTrigger(driver).controllerId shouldBe playerA  // permanent controller == active player here
    }
})
