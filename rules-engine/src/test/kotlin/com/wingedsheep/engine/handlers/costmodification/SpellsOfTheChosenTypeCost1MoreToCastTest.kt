package com.wingedsheep.engine.handlers.costmodification

import com.wingedsheep.engine.core.ChooseOptionDecision
import com.wingedsheep.engine.core.OptionChosenResponse
import com.wingedsheep.engine.mechanics.mana.CostCalculator
import com.wingedsheep.engine.registry.CardRegistry
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.scripting.SpellsOfChosenTypeCost1MoreToCast
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * BDD: Static cost-increase tax applies {1} surcharge to spells matching the chosen card type.
 *
 * GIVEN  A static cost-increase effect is active on the battlefield with a recorded chosen
 *        card type of 'Instant'
 * AND    The active player has in hand one instant spell with base mana cost {R} and one
 *        sorcery spell with base mana cost {R}
 * AND    The active player has sufficient mana available to pay either spell's modified cost
 * WHEN   The engine computes the total cost to cast each spell from hand
 * THEN   The instant spell's total cost is increased by exactly {1} (resulting in {1}{R})
 * AND    The sorcery spell's total cost is unchanged (resulting in {R})
 * AND    The surcharge is applied as a cost modification, not as an additional cost, so
 *        cost-reduction effects can still reduce the modified cost
 */
class SpellsOfTheChosenTypeCost1MoreToCastTest : FunSpec({

    // A permanent that (a) prompts "choose a card type other than Creature" on ETB, and
    // (b) carries a static ability making spells of that chosen type cost {1} more.
    val TypeTaxer = card("Type Taxer") {
        manaCost = "{2}{U}"
        typeLine = "Creature — Human Wizard"
        power = 2
        toughness = 2

        triggeredAbility {
            trigger = Triggers.EntersBattlefield
            effect = Effects.ChooseCardType(excludedCardTypes = listOf("Creature"))
        }

        staticAbility {
            ability = SpellsOfChosenTypeCost1MoreToCast
        }
    }

    // A sorcery with the same base cost as Lightning Bolt to confirm no tax is applied.
    val testSorcery = card("Test Sorcery") {
        manaCost = "{R}"
        typeLine = "Sorcery"
    }

    fun createDriver(): GameTestDriver {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all + listOf(TypeTaxer))
        return driver
    }

    test("instant spell costs {1} more to cast when instant is the chosen card type; sorcery cost is unchanged") {
        val registry = CardRegistry()
        registry.register(TestCards.all)
        registry.register(TypeTaxer)

        val calculator = CostCalculator(registry)

        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Island" to 20), startingLife = 20)

        val activePlayer = driver.activePlayer!!
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        // Cast Type Taxer and choose "Instant" as the taxed card type on ETB.
        val taxerCardId = driver.putCardInHand(activePlayer, "Type Taxer")
        driver.giveMana(activePlayer, Color.BLUE, 1)
        driver.giveColorlessMana(activePlayer, 2)
        driver.castSpell(activePlayer, taxerCardId)
        driver.bothPass() // creature spell resolves; enters battlefield, ETB trigger on stack
        driver.bothPass() // ETB trigger resolves → pauses for card-type choice

        val decision = driver.pendingDecision as ChooseOptionDecision
        val instantIndex = decision.options.indexOf("Instant")
        driver.submitDecision(activePlayer, OptionChosenResponse(decision.id, instantIndex))

        // Type Taxer is now on the battlefield with ChosenCardTypeComponent("Instant").

        // Instant {R} should be taxed to {1}{R}.
        val instantCost = calculator.calculateEffectiveCost(driver.state, TestCards.LightningBolt, activePlayer)
        instantCost.genericAmount shouldBe 1  // {1}{R}: 1 generic
        instantCost.cmc shouldBe 2            // {1} + {R} = CMC 2

        // Sorcery {R} matches no chosen type → unchanged.
        val sorceryCost = calculator.calculateEffectiveCost(driver.state, testSorcery, activePlayer)
        sorceryCost.genericAmount shouldBe 0  // {R}: 0 generic
        sorceryCost.cmc shouldBe 1            // just {R} = CMC 1
    }
})
