package com.wingedsheep.engine.abilitywords

import com.wingedsheep.engine.core.ActivateAbility
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Deck
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * BDD test for the Nosh ability word.
 *
 * Nosh is a pure flavor marker (ability word) — it italicizes and groups abilities
 * thematically but carries no rules meaning of its own. A Nosh-tagged ability must
 * resolve exactly like the same ability without the prefix.
 */
class NoshAbilityWordTest : FunSpec({

    // Card whose activated ability is prefixed with the Nosh ability word in oracle text.
    val noshArtifact = card("Nosh Artifact") {
        manaCost = "{0}"
        typeLine = "Artifact"
        oracleText = "{2}, {T}, Sacrifice this artifact: Nosh — You gain 3 life and draw a card."
        activatedAbility {
            cost = Costs.Composite(Costs.Mana("{2}"), Costs.Tap, Costs.SacrificeSelf)
            effect = Effects.Composite(
                Effects.GainLife(3),
                Effects.DrawCards(1),
            )
        }
    }

    // Identical card without the Nosh prefix — used as a control to verify pass-through.
    val plainArtifact = card("Plain Artifact") {
        manaCost = "{0}"
        typeLine = "Artifact"
        oracleText = "{2}, {T}, Sacrifice this artifact: You gain 3 life and draw a card."
        activatedAbility {
            cost = Costs.Composite(Costs.Mana("{2}"), Costs.Tap, Costs.SacrificeSelf)
            effect = Effects.Composite(
                Effects.GainLife(3),
                Effects.DrawCards(1),
            )
        }
    }

    val noshAbilityId = noshArtifact.activatedAbilities.first().id
    val plainAbilityId = plainArtifact.activatedAbilities.first().id

    fun createDriver(): GameTestDriver {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all)
        driver.registerCard(noshArtifact)
        driver.registerCard(plainArtifact)
        return driver
    }

    test("Nosh ability word is purely a flavor marker and does not alter ability resolution") {
        // GIVEN an activated ability tagged with the Nosh ability word
        // AND the ability's cost ({2}, {T}, Sacrifice this artifact) and effect
        //     (gain 3 life, draw 1 card) are otherwise well-formed and legal

        // The engine must recognise "Nosh" as a known ability word before any
        // card carrying the prefix can be registered without error.
        val noshRecognised = Keyword.entries.any { it.displayName == "Nosh" }
        noshRecognised shouldBe true   // fails until Keyword.NOSH is added + handler wired

        // WHEN the Nosh-tagged ability is activated and resolves
        val driver = createDriver()
        driver.initMirrorMatch(
            deck = Deck.of("Forest" to 60),
            startingLife = 20
        )
        val activePlayer = driver.activePlayer!!
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        // Put the Nosh artifact on the battlefield, then activate its ability
        val noshPermanent = driver.putPermanentOnBattlefield(activePlayer, "Nosh Artifact")

        // Activate the Nosh-tagged ability
        driver.giveColorlessMana(activePlayer, 2)
        val lifeBefore = driver.getLifeTotal(activePlayer)
        val handBefore = driver.getHandSize(activePlayer)
        val noshResult = driver.submit(
            ActivateAbility(playerId = activePlayer, sourceId = noshPermanent, abilityId = noshAbilityId)
        )
        noshResult.isSuccess shouldBe true
        driver.bothPass()
        val lifeGainedByNosh = driver.getLifeTotal(activePlayer) - lifeBefore
        val drawnByNosh = driver.getHandSize(activePlayer) - handBefore

        // Activate the plain (control) ability — put a fresh artifact and add mana
        val plainPermanent2 = driver.putPermanentOnBattlefield(activePlayer, "Plain Artifact")
        driver.giveColorlessMana(activePlayer, 2)
        val lifeBefore2 = driver.getLifeTotal(activePlayer)
        val handBefore2 = driver.getHandSize(activePlayer)
        val plainResult = driver.submit(
            ActivateAbility(playerId = activePlayer, sourceId = plainPermanent2, abilityId = plainAbilityId)
        )
        plainResult.isSuccess shouldBe true
        driver.bothPass()
        val lifeGainedByPlain = driver.getLifeTotal(activePlayer) - lifeBefore2
        val drawnByPlain = driver.getHandSize(activePlayer) - handBefore2

        // THEN Nosh prefix changes nothing — both abilities produce identical outcomes
        lifeGainedByNosh shouldBe lifeGainedByPlain
        lifeGainedByNosh shouldBe 3
        drawnByNosh shouldBe drawnByPlain
        drawnByNosh shouldBe 1
    }
})
