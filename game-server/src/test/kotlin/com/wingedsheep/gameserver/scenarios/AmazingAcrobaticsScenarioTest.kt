package com.wingedsheep.gameserver.scenarios

import com.wingedsheep.engine.core.CastSpell
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.state.components.stack.ChosenTarget
import com.wingedsheep.gameserver.ScenarioTestBase
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Amazing Acrobatics.
 *
 * Card reference:
 * - Amazing Acrobatics ({1}{U}{U}): Instant
 *   "Choose one or both —
 *    • Counter target spell.
 *    • Tap up to two target creatures."
 */
class AmazingAcrobaticsScenarioTest : ScenarioTestBase() {

    init {
        context("Amazing Acrobatics — counter mode only") {

            test("counters the target spell and both spells resolve to graveyard") {
                // P2 is active so they can cast a creature at sorcery speed; P1 responds with Amazing Acrobatics
                val game = scenario()
                    .withPlayers("Caster", "Opponent")
                    .withActivePlayer(2)
                    .withCardInHand(1, "Amazing Acrobatics")
                    .withLandsOnBattlefield(1, "Island", 3)
                    .withCardInHand(2, "Grizzly Bears")
                    .withLandsOnBattlefield(2, "Forest", 2)
                    .withCardInLibrary(1, "Island")
                    .withCardInLibrary(2, "Forest")
                    .build()

                // Opponent casts a spell; it lands on the stack
                game.castSpell(2, "Grizzly Bears")
                // P2 passes priority; P1 now has priority with Amazing Acrobatics in hand
                game.passPriority()

                val spellOnStack = game.state.stack.find { entityId ->
                    game.state.getEntity(entityId)?.get<CardComponent>()?.name == "Grizzly Bears"
                }!!

                val acrobaticsId = game.state.getHand(game.player1Id).find { entityId ->
                    game.state.getEntity(entityId)?.get<CardComponent>()?.name == "Amazing Acrobatics"
                }!!

                // Cast Amazing Acrobatics choosing only the 'Counter target spell' mode (mode index 0)
                val result = game.execute(
                    CastSpell(
                        playerId = game.player1Id,
                        cardId = acrobaticsId,
                        targets = listOf(ChosenTarget.Spell(spellOnStack)),
                        chosenModes = listOf(0),
                        modeTargetsOrdered = listOf(listOf(ChosenTarget.Spell(spellOnStack)))
                    )
                )
                withClue("Casting Amazing Acrobatics should succeed: ${result.error}") {
                    result.isSuccess shouldBe true
                }

                game.resolveStack()

                withClue("Grizzly Bears should be countered and in opponent's graveyard") {
                    game.isInGraveyard(2, "Grizzly Bears") shouldBe true
                }
                withClue("Grizzly Bears should not be on the battlefield") {
                    game.isOnBattlefield("Grizzly Bears") shouldBe false
                }
                withClue("Amazing Acrobatics should be in its owner's graveyard after resolution") {
                    game.isInGraveyard(1, "Amazing Acrobatics") shouldBe true
                }
            }
        }
    }
}
