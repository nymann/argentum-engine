package com.wingedsheep.engine.abilitywords

import com.wingedsheep.engine.core.ActivateAbility
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.scripting.effects.DrawCardsEffect
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * BDD test for the Share ability word.
 *
 * Share is a pure flavor marker (ability word) — it italicizes and groups abilities
 * thematically but carries no rules meaning of its own. A Share-tagged ability must
 * resolve exactly like the same ability without the prefix.
 */
class ShareAbilityWordTest : FunSpec({

    // Card whose activated ability is prefixed with the Share ability word in oracle text.
    val shareTapper = card("Share Tapper") {
        manaCost = "{0}"
        typeLine = "Artifact"
        oracleText = "{T}: Share — Draw a card."
        activatedAbility {
            cost = Costs.Tap
            effect = DrawCardsEffect(count = 1, target = EffectTarget.Controller)
        }
    }

    // Identical card without the Share prefix — used as a control to verify pass-through.
    val plainTapper = card("Plain Tapper") {
        manaCost = "{0}"
        typeLine = "Artifact"
        oracleText = "{T}: Draw a card."
        activatedAbility {
            cost = Costs.Tap
            effect = DrawCardsEffect(count = 1, target = EffectTarget.Controller)
        }
    }

    val shareAbilityId = shareTapper.activatedAbilities.first().id
    val plainAbilityId = plainTapper.activatedAbilities.first().id

    fun createDriver(): GameTestDriver {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all)
        driver.registerCard(shareTapper)
        driver.registerCard(plainTapper)
        return driver
    }

    test("Share ability word is purely a flavor marker and does not alter ability resolution") {
        // GIVEN an activated ability tagged with the Share ability word
        // AND the ability's cost and effect are otherwise well-formed and legal

        // The engine must recognise "Share" as a known ability word before any
        // card carrying the prefix can be registered without error.
        val shareRecognised = Keyword.entries.any { it.displayName == "Share" }
        shareRecognised shouldBe true   // fails until Keyword.SHARE is added + handler wired

        // WHEN the Share-tagged ability is activated and resolves
        val driver = createDriver()
        driver.initMirrorMatch(
            deck = Deck.of("Forest" to 60),
            startingLife = 20
        )
        val activePlayer = driver.activePlayer!!
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        val sharedArtifact = driver.putPermanentOnBattlefield(activePlayer, "Share Tapper")
        val plainArtifact  = driver.putPermanentOnBattlefield(activePlayer, "Plain Tapper")

        val handBefore = driver.getHandSize(activePlayer)
        val shareResult = driver.submit(
            ActivateAbility(playerId = activePlayer, sourceId = sharedArtifact, abilityId = shareAbilityId)
        )
        shareResult.isSuccess shouldBe true
        driver.bothPass()
        val drawnByShare = driver.getHandSize(activePlayer) - handBefore

        // Control: identical ability without Share prefix
        val handBefore2 = driver.getHandSize(activePlayer)
        val plainResult = driver.submit(
            ActivateAbility(playerId = activePlayer, sourceId = plainArtifact, abilityId = plainAbilityId)
        )
        plainResult.isSuccess shouldBe true
        driver.bothPass()
        val drawnByPlain = driver.getHandSize(activePlayer) - handBefore2

        // THEN Share prefix changes nothing — both abilities draw the same cards
        drawnByShare shouldBe drawnByPlain
        drawnByShare shouldBe 1
    }
})
