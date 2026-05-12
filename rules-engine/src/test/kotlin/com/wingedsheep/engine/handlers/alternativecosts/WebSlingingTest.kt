package com.wingedsheep.engine.handlers.alternativecosts

import com.wingedsheep.engine.core.CastSpell
import com.wingedsheep.engine.core.PaymentStrategy
import com.wingedsheep.engine.state.ZoneKey
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.scripting.AdditionalCost
import com.wingedsheep.sdk.scripting.AdditionalCostPayment
import com.wingedsheep.sdk.scripting.SelfAlternativeCost
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe

/**
 * BDD test for the Web-slinging alternative-cost keyword.
 *
 * Web-slinging {W}: you may cast this spell by paying {W} and returning a tapped creature
 * you control to its owner's hand, rather than paying the spell's printed mana cost.
 */
class WebSlingingTest : FunSpec({

    val WebSlingerSpell = card("Web Slinger Spell") {
        manaCost = "{2}{W}"
        typeLine = "Instant"
        oracleText = "Web-slinging {W} (You may pay {W} and return a tapped creature you control to its owner's hand rather than pay this spell's mana cost.) Draw a card."

        spell { effect = Effects.DrawCards(1) }

        selfAlternativeCost = SelfAlternativeCost(
            manaCost = "{W}",
            additionalCosts = listOf(AdditionalCost.ReturnTappedCreatureToHand())
        )
    }

    fun createDriver(): GameTestDriver {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all + listOf(WebSlingerSpell))
        return driver
    }

    test("web-slinging alternative cost pays {W} and bounces the designated tapped creature to its owner's hand") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Plains" to 20))

        val active = driver.activePlayer!!
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        // GIVEN: a tapped creature the active player controls on the battlefield
        val tappedCreature = driver.putCreatureOnBattlefield(active, "Savannah Lions")
        driver.tapPermanent(tappedCreature)

        // GIVEN: the Web-slinging spell in hand
        val spell = driver.putCardInHand(active, "Web Slinger Spell")

        // GIVEN: exactly one untapped Plains (enough for {W} but not the printed {2}{W})
        val plains = driver.putLandOnBattlefield(active, "Plains")

        // WHEN: cast via Web-slinging alternative cost, designating the tapped creature to return
        val result = driver.submit(
            CastSpell(
                playerId = active,
                cardId = spell,
                useAlternativeCost = true,
                paymentStrategy = PaymentStrategy.AutoPay,
                additionalCostPayment = AdditionalCostPayment(bouncedPermanents = listOf(tappedCreature))
            )
        )

        // THEN: cast succeeds and spell is on the stack
        result.isSuccess shouldBe true
        driver.state.stack shouldContain spell

        // THEN: the one Plains is tapped — the alternative {W} cost was paid, no generic mana needed
        driver.isTapped(plains) shouldBe true

        // THEN: the tapped creature is now in its owner's hand, no longer on the battlefield
        driver.state.getZone(ZoneKey(active, Zone.HAND)) shouldContain tappedCreature
        driver.state.getZone(ZoneKey(active, Zone.BATTLEFIELD)) shouldNotContain tappedCreature
    }

    test("web-slinging alternative cost is rejected when no tapped controlled creature is designated") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Plains" to 20))

        val active = driver.activePlayer!!
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        // GIVEN: only an untapped creature on the battlefield — no legal tapped creature to return
        driver.putCreatureOnBattlefield(active, "Savannah Lions")

        // GIVEN: the Web-slinging spell in hand with one Plains available
        val spell = driver.putCardInHand(active, "Web Slinger Spell")
        driver.putLandOnBattlefield(active, "Plains")

        // WHEN: attempt to cast via Web-slinging without designating any creature to return
        val result = driver.submit(
            CastSpell(
                playerId = active,
                cardId = spell,
                useAlternativeCost = true,
                paymentStrategy = PaymentStrategy.AutoPay,
                additionalCostPayment = AdditionalCostPayment()
            )
        )

        // THEN: the cast is rejected — no tapped controlled creature was designated for the bounce cost
        result.isSuccess shouldBe false
    }
})
