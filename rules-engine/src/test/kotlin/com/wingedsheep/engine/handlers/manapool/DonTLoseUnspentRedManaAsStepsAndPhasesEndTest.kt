package com.wingedsheep.engine.handlers.manapool

import com.wingedsheep.engine.state.ComponentContainer
import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.components.player.ManaPoolComponent
import com.wingedsheep.engine.state.components.player.PreserveUnspentManaOfColorComponent
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.model.EntityId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * BDD spec for Rule 500.4 colour-selective mana preservation.
 *
 * "You don't lose unspent red mana as steps and phases end."
 *
 * No card definitions or set registration — the synthetic modifier is applied
 * directly as a [PreserveUnspentManaOfColorComponent] on the player entity.
 */
class DonTLoseUnspentRedManaAsStepsAndPhasesEndTest : FunSpec({

    val handler = PreserveUnspentManaHandler()

    test("controller with preserve-red retains red mana, loses non-red; opponent loses all mana including red") {
        // GIVEN a player who controls a permanent with
        //   "You don't lose unspent red mana as steps and phases end"
        val p1 = EntityId.generate()
        val p2 = EntityId.generate()

        val p1Container = ComponentContainer.of(
            ManaPoolComponent(red = 2, green = 1),
            PreserveUnspentManaOfColorComponent(Color.RED)
        )
        val p2Container = ComponentContainer.of(
            ManaPoolComponent(red = 3, green = 1)
        )

        val state = GameState(
            entities = mapOf(p1 to p1Container, p2 to p2Container),
            turnOrder = listOf(p1, p2),
            activePlayerId = p1
        )

        // WHEN the step/phase ends and the engine performs the Rule 500.4
        // empty-mana-pools turn-based action
        val result = handler.emptyManaPools(state)

        val p1Pool = result.getEntity(p1)!!.get<ManaPoolComponent>()!!
        val p2Pool = result.getEntity(p2)!!.get<ManaPoolComponent>()!!

        // THEN the controller retains all unspent red mana
        p1Pool.red shouldBe 2

        // AND all non-red mana in that pool is emptied as normal
        p1Pool.green shouldBe 0

        // AND the opponent's entire pool is emptied, including red
        p2Pool.red shouldBe 0
        p2Pool.green shouldBe 0
    }
})
