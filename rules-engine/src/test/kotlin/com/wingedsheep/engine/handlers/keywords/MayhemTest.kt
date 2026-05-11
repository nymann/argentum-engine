package com.wingedsheep.engine.handlers.keywords

import com.wingedsheep.engine.core.CastSpell
import com.wingedsheep.engine.legalactions.support.setupP1
import com.wingedsheep.engine.legalactions.support.shouldContainCastOf
import com.wingedsheep.engine.legalactions.support.shouldNotContainCastOf
import com.wingedsheep.engine.state.ZoneKey
import com.wingedsheep.engine.state.components.battlefield.DiscardedThisTurnComponent
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.sdk.core.ManaCost
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.model.CardDefinition
import com.wingedsheep.sdk.scripting.KeywordAbility
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

/**
 * BDD tests for the Mayhem keyword.
 *
 * Mayhem {3}{B} — You may cast this card from your graveyard for its mayhem cost,
 * but only if you discarded it this turn. Then exile it.
 *
 * Synthetic test card defined inline; no set file is touched.
 */
class MayhemTest : FunSpec({

    val MayhemTestCard = CardDefinition.sorcery(
        name = "Mayhem Test Card",
        manaCost = ManaCost.parse("{5}{B}"),
        oracleText = "Mayhem {3}{B} (You may cast this card from your graveyard for its mayhem cost if you discarded it this turn. Then exile it.)"
    ).copy(keywordAbilities = listOf(KeywordAbility.Mayhem(ManaCost.parse("{3}{B}"))))

    context("Mayhem (cast from graveyard when discarded this turn)") {

        test(
            "GIVEN card with Mayhem in graveyard discarded this turn AND sufficient mana " +
                "WHEN legal actions are enumerated " +
                "THEN a CastWithMayhem action at cost {3}{B} is included"
        ) {
            val driver = setupP1(
                battlefield = listOf("Swamp", "Swamp", "Swamp", "Island"),
                graveyard = listOf("Mayhem Test Card"),
                extraSetCards = listOf(MayhemTestCard)
            )

            // Stamp DiscardedThisTurnComponent — simulates the card having been discarded this turn.
            val cardId = driver.game.state
                .getZone(ZoneKey(driver.player1, Zone.GRAVEYARD))
                .first { id ->
                    driver.game.state.getEntity(id)?.get<CardComponent>()?.name == "Mayhem Test Card"
                }
            val stamped = driver.game.state.getEntity(cardId)!!
                .with(DiscardedThisTurnComponent(turnNumber = driver.game.state.turnNumber))
            driver.game.replaceState(driver.game.state.withEntity(cardId, stamped))

            val view = driver.enumerateFor(driver.player1)

            view shouldContainCastOf "Mayhem Test Card"
            val mayhem = view.castActionsFor("Mayhem Test Card").first()
            mayhem.actionType shouldBe "CastWithMayhem"
            mayhem.affordable shouldBe true
            mayhem.manaCostString shouldBe "{3}{B}"
            mayhem.sourceZone shouldBe "GRAVEYARD"
            (mayhem.action as CastSpell).useAlternativeCost shouldBe true
        }

        test(
            "GIVEN card with Mayhem in graveyard but NOT discarded this turn (e.g. milled) " +
                "WHEN legal actions are enumerated " +
                "THEN no CastWithMayhem action is offered"
        ) {
            val driver = setupP1(
                battlefield = listOf("Swamp", "Swamp", "Swamp", "Island"),
                graveyard = listOf("Mayhem Test Card"),
                extraSetCards = listOf(MayhemTestCard)
            )
            // No DiscardedThisTurnComponent — card entered graveyard via mill.

            val mayhemActions = driver.enumerateFor(driver.player1)
                .filter { it.actionType == "CastWithMayhem" }

            mayhemActions shouldHaveSize 0
        }

        test(
            "GIVEN card with Mayhem in graveyard discarded on a prior turn " +
                "WHEN legal actions are enumerated " +
                "THEN no CastWithMayhem action is offered"
        ) {
            val driver = setupP1(
                battlefield = listOf("Swamp", "Swamp", "Swamp", "Island"),
                graveyard = listOf("Mayhem Test Card"),
                extraSetCards = listOf(MayhemTestCard)
            )

            // Stamp with a prior turn number — discard happened a turn ago.
            val cardId = driver.game.state
                .getZone(ZoneKey(driver.player1, Zone.GRAVEYARD))
                .first { id ->
                    driver.game.state.getEntity(id)?.get<CardComponent>()?.name == "Mayhem Test Card"
                }
            val priorTurn = driver.game.state.turnNumber - 1
            val staleStamp = driver.game.state.getEntity(cardId)!!
                .with(DiscardedThisTurnComponent(turnNumber = priorTurn))
            driver.game.replaceState(driver.game.state.withEntity(cardId, staleStamp))

            val mayhemActions = driver.enumerateFor(driver.player1)
                .filter { it.actionType == "CastWithMayhem" }

            mayhemActions shouldHaveSize 0
        }
    }
})
