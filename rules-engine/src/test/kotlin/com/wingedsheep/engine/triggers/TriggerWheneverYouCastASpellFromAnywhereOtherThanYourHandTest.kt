package com.wingedsheep.engine.triggers

import com.wingedsheep.engine.core.AbilityTriggeredEvent
import com.wingedsheep.engine.legalactions.support.setupP1
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.ManaCost
import com.wingedsheep.sdk.core.TypeLine
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.model.CardDefinition
import com.wingedsheep.sdk.model.CardScript
import com.wingedsheep.sdk.scripting.GameEvent
import com.wingedsheep.sdk.scripting.KeywordAbility
import com.wingedsheep.sdk.scripting.TriggeredAbility
import com.wingedsheep.sdk.scripting.TriggerBinding
import com.wingedsheep.sdk.scripting.effects.GainLifeEffect
import com.wingedsheep.sdk.scripting.references.Player
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Verifies the trigger-filter predicate "cast from anywhere other than your hand":
 * a trigger with [GameEvent.SpellCastEvent.castFromZoneOtherThan] = HAND must fire
 * exactly once for a graveyard cast and zero times for a hand cast.
 */
class TriggerWheneverYouCastASpellFromAnywhereOtherThanYourHandTest : FunSpec({

    val castNonHandWatcher = CardDefinition.enchantment(
        name = "Cast Non-Hand Watcher",
        manaCost = ManaCost.parse("{2}"),
        oracleText = "Whenever you cast a spell from anywhere other than your hand, you gain 1 life.",
        script = CardScript.permanent(
            triggeredAbilities = listOf(
                TriggeredAbility.create(
                    trigger = GameEvent.SpellCastEvent(
                        player = Player.You,
                        castFromZoneOtherThan = Zone.HAND
                    ),
                    binding = TriggerBinding.ANY,
                    effect = GainLifeEffect(1)
                )
            )
        )
    )

    val testGraveyardSpell = CardDefinition(
        name = "Test Graveyard Spell",
        manaCost = ManaCost.parse("{1}{U}"),
        typeLine = TypeLine.sorcery(),
        oracleText = "You gain 1 life. Flashback {1}{U}.",
        keywordAbilities = listOf(KeywordAbility.Flashback(ManaCost.parse("{1}{U}"))),
        script = CardScript.spell(effect = GainLifeEffect(1))
    )

    val testHandSpell = CardDefinition.instant(
        name = "Test Hand Spell",
        manaCost = ManaCost.parse("{R}"),
        oracleText = "You gain 1 life.",
        script = CardScript.spell(effect = GainLifeEffect(1))
    )

    val extraCards = listOf(castNonHandWatcher, testGraveyardSpell, testHandSpell)

    test("trigger fires for graveyard cast but not for hand cast") {
        // GIVEN: watcher on battlefield, castable spell in graveyard via flashback
        val graveyardDriver = setupP1(
            battlefield = listOf("Cast Non-Hand Watcher"),
            graveyard = listOf("Test Graveyard Spell"),
            extraSetCards = extraCards
        )
        graveyardDriver.game.giveMana(graveyardDriver.player1, Color.BLUE, 2)

        val flashbackAction = graveyardDriver.enumerateFor(graveyardDriver.player1)
            .castActionsFor("Test Graveyard Spell").first().action

        // WHEN: controller casts from graveyard
        val graveyardResult = graveyardDriver.game.submit(flashbackAction)

        // THEN: exactly one trigger is queued for the watcher
        val graveyardTriggers = graveyardResult.events
            .filterIsInstance<AbilityTriggeredEvent>()
            .filter { it.sourceName == "Cast Non-Hand Watcher" }
        graveyardTriggers.size shouldBe 1

        // GIVEN: fresh state — watcher on battlefield, spell in hand
        val handDriver = setupP1(
            battlefield = listOf("Cast Non-Hand Watcher"),
            hand = listOf("Test Hand Spell"),
            extraSetCards = extraCards
        )
        handDriver.game.giveMana(handDriver.player1, Color.RED, 1)

        val handCastAction = handDriver.enumerateFor(handDriver.player1)
            .castActionsFor("Test Hand Spell").first().action

        // WHEN: controller casts from hand
        val handResult = handDriver.game.submit(handCastAction)

        // THEN: no trigger fires for the hand cast
        val handTriggers = handResult.events
            .filterIsInstance<AbilityTriggeredEvent>()
            .filter { it.sourceName == "Cast Non-Hand Watcher" }
        handTriggers.size shouldBe 0
    }
})
