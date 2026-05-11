package com.wingedsheep.engine.handlers.cast

import com.wingedsheep.engine.core.CastSpell
import com.wingedsheep.engine.core.PaymentStrategy
import com.wingedsheep.engine.state.ZoneKey
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.scripting.AdditionalCostPayment
import com.wingedsheep.sdk.scripting.MayCastFromGraveyardWithAdditionalDiscardCost
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe

/**
 * BDD test for the 'cast-from-graveyard-with-additional-discard-cost' alternative cast permission.
 *
 * Unlike Flashback, this permission:
 *   - uses the spell's printed mana cost (no alternative cost)
 *   - requires discarding a card as an additional cost
 *   - does NOT exile the spell on resolution (normal graveyard destination)
 */
class CastFromGraveyardWithAdditionalDiscardCostAlternativeCastPermissionTest : FunSpec({

    val GraveyardDiscardSpell = card("Graveyard Discard Spell") {
        manaCost = "{1}{U}"
        typeLine = "Sorcery"
        oracleText = "You may cast this spell from your graveyard by discarding a card as an additional cost. Draw a card."

        spell { effect = Effects.DrawCards(1) }

        staticAbility {
            ability = MayCastFromGraveyardWithAdditionalDiscardCost(discardCount = 1)
        }
    }

    fun createDriver(): GameTestDriver {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all + listOf(GraveyardDiscardSpell))
        return driver
    }

    test("casting a graveyard spell via the discard-a-card permission moves it to the stack not exile and discards a hand card") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Island" to 20))

        val active = driver.activePlayer!!
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        // GIVEN: spell in graveyard, mana available, a hand card to discard
        val spell = driver.putCardInGraveyard(active, "Graveyard Discard Spell")
        val handCard = driver.putCardInHand(active, "Island")
        driver.giveMana(active, Color.BLUE, 2)

        // WHEN: cast the graveyard spell via the discard permission
        val result = driver.submit(
            CastSpell(
                playerId = active,
                cardId = spell,
                additionalCostPayment = AdditionalCostPayment(discardedCards = listOf(handCard)),
                paymentStrategy = PaymentStrategy.FromPool
            )
        )

        // THEN: cast succeeds
        result.isSuccess shouldBe true

        // THEN: spell is on the stack, NOT exiled by the act of casting
        driver.state.stack shouldContain spell
        driver.state.getZone(ZoneKey(active, Zone.EXILE)) shouldNotContain spell
        driver.state.getZone(ZoneKey(active, Zone.GRAVEYARD)) shouldNotContain spell

        // THEN: the discarded hand card is in the graveyard (cost paid before stack placement)
        driver.state.getZone(ZoneKey(active, Zone.GRAVEYARD)) shouldContain handCard
        driver.state.getZone(ZoneKey(active, Zone.HAND)) shouldNotContain handCard

        // WHEN: spell resolves
        driver.bothPass()

        // THEN: after resolution the spell goes to graveyard (no Flashback-style exile side effect)
        driver.state.getZone(ZoneKey(active, Zone.GRAVEYARD)) shouldContain spell
        driver.state.getZone(ZoneKey(active, Zone.EXILE)) shouldNotContain spell
    }
})
