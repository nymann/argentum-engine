package com.wingedsheep.engine.handlers.effects

import com.wingedsheep.engine.core.CardTypeChosenEvent
import com.wingedsheep.engine.core.ChooseOptionDecision
import com.wingedsheep.engine.core.OptionChosenResponse
import com.wingedsheep.engine.state.components.identity.ChosenCardTypeComponent
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.sdk.core.CardType
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Deck
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * BDD: ETB prompts active player to choose a non-creature card type and records the choice.
 *
 * GIVEN  A permanent enters the battlefield under the active player's control with an ETB
 *        effect that triggers the 'choose a card type other than creature' primitive
 * AND    The player-input protocol is wired to return 'Instant'
 * AND    The legal options exclude 'Creature' and include all other card types
 * WHEN   The ETB trigger resolves
 * THEN   The engine emits CardTypeChosenEvent(chosenType = "Instant")
 * AND    The option set did NOT contain "Creature"
 * AND    The game state stores the chosen type on ChosenCardTypeComponent on the source entity
 */
class EtbChooseACardTypeOtherThanCreatureTest : FunSpec({

    val EtbChooseCardType = card("ETB Choose Card Type") {
        manaCost = "{2}{U}"
        typeLine = "Creature — Human Wizard"
        power = 2
        toughness = 2

        triggeredAbility {
            trigger = Triggers.EntersBattlefield
            effect = Effects.ChooseCardType(excludedCardTypes = listOf("Creature"))
        }
    }

    fun createDriver(): GameTestDriver {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all + listOf(EtbChooseCardType))
        return driver
    }

    test("ETB trigger pauses for card-type choice and presents only non-creature types") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Island" to 20, "Plains" to 20))

        val activePlayer = driver.activePlayer!!
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        val cardId = driver.putCardInHand(activePlayer, "ETB Choose Card Type")
        driver.giveMana(activePlayer, Color.BLUE, 2)
        driver.giveMana(activePlayer, Color.WHITE, 1)
        driver.castSpell(activePlayer, cardId)
        driver.bothPass() // creature spell resolves, enters battlefield; ETB trigger on stack
        driver.bothPass() // ETB trigger resolves → pauses for card-type choice

        val decision = driver.pendingDecision
        decision shouldNotBe null
        decision.shouldBeInstanceOf<ChooseOptionDecision>()

        // Creature must not appear in the offered options
        decision.options.contains("Creature") shouldBe false

        // Every non-creature card type must be present
        val nonCreatureTypes = CardType.entries
            .filter { it != CardType.CREATURE }
            .map { it.displayName }
        nonCreatureTypes.forEach { typeName ->
            decision.options.contains(typeName) shouldBe true
        }
    }

    test("choosing Instant emits CardTypeChosenEvent and attaches ChosenCardTypeComponent to the source") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Island" to 20, "Plains" to 20))

        val activePlayer = driver.activePlayer!!
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        val cardId = driver.putCardInHand(activePlayer, "ETB Choose Card Type")
        driver.giveMana(activePlayer, Color.BLUE, 2)
        driver.giveMana(activePlayer, Color.WHITE, 1)
        driver.castSpell(activePlayer, cardId)
        driver.bothPass() // creature enters battlefield
        driver.bothPass() // trigger resolves → pauses for choice

        val decision = driver.pendingDecision as ChooseOptionDecision
        val instantIndex = decision.options.indexOf("Instant")
        driver.submitDecision(activePlayer, OptionChosenResponse(decision.id, instantIndex))

        // CardTypeChosenEvent must have been emitted with the chosen type
        val event = driver.events.filterIsInstance<CardTypeChosenEvent>().lastOrNull()
        event shouldNotBe null
        event!!.chosenType shouldBe "Instant"

        // ChosenCardTypeComponent must be attached to the permanent that entered
        val permanentId = driver.findPermanent(activePlayer, "ETB Choose Card Type")
        permanentId shouldNotBe null
        val component = driver.state.getEntity(permanentId!!)?.get<ChosenCardTypeComponent>()
        component shouldNotBe null
        component!!.chosenCardType shouldBe "Instant"
    }
})
