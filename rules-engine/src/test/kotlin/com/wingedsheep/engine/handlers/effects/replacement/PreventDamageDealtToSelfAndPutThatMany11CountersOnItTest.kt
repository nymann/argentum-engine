package com.wingedsheep.engine.handlers.effects.replacement

import com.wingedsheep.engine.core.CountersAddedEvent
import com.wingedsheep.engine.state.components.battlefield.CountersComponent
import com.wingedsheep.engine.state.components.battlefield.DamageComponent
import com.wingedsheep.engine.state.components.battlefield.ReplacementEffectSourceComponent
import com.wingedsheep.engine.state.components.stack.ChosenTarget
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.CounterType
import com.wingedsheep.sdk.core.ManaCost
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.core.Subtype
import com.wingedsheep.sdk.model.CardDefinition
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.scripting.GameEvent.DamageEvent
import com.wingedsheep.sdk.scripting.PreventDamage
import com.wingedsheep.sdk.scripting.events.DamageType
import com.wingedsheep.sdk.scripting.events.RecipientFilter
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class PreventDamageDealtToSelfAndPutThatMany11CountersOnItTest : FunSpec({

    val damageToDeal = 3

    val SelfProtectingCreature = CardDefinition.creature(
        name = "Self Protecting Creature",
        manaCost = ManaCost.parse("{2}{G}"),
        subtypes = setOf(Subtype("Beast")),
        power = 1,
        toughness = 3,
        oracleText = "If damage would be dealt to this creature, prevent that damage and put that many +1/+1 counters on it instead."
    )

    fun createDriver(): GameTestDriver {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all + listOf(SelfProtectingCreature))
        return driver
    }

    test("prevented damage is converted to +1/+1 counters on the self-protecting creature") {
        val driver = createDriver()
        driver.initMirrorMatch(
            deck = Deck.of("Forest" to 20, "Mountain" to 20),
            startingLife = 20
        )

        val activePlayer = driver.activePlayer!!
        val opponent = driver.getOpponent(activePlayer)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        val creature = driver.putCreatureOnBattlefield(activePlayer, "Self Protecting Creature")

        driver.replaceState(
            driver.state.updateEntity(creature) { container ->
                container.with(
                    ReplacementEffectSourceComponent(
                        listOf(
                            PreventDamage(
                                amount = null,
                                appliesTo = DamageEvent(
                                    recipient = RecipientFilter.Self,
                                    damageType = DamageType.Any
                                )
                            )
                        )
                    )
                )
            }
        )

        val initialDamage = driver.state.getEntity(creature)?.get<DamageComponent>()?.amount ?: 0
        initialDamage shouldBe 0
        val initialCounters = driver.state.getEntity(creature)
            ?.get<CountersComponent>()?.getCount(CounterType.PLUS_ONE_PLUS_ONE) ?: 0
        initialCounters shouldBe 0

        driver.giveMana(opponent, Color.RED, 1)
        val bolt = driver.putCardInHand(opponent, "Lightning Bolt")
        driver.passPriority(activePlayer)
        driver.castSpellWithTargets(
            opponent,
            bolt,
            listOf(ChosenTarget.Permanent(creature))
        )
        driver.bothPass()

        val markedDamage = driver.state.getEntity(creature)?.get<DamageComponent>()?.amount ?: 0
        markedDamage shouldBe 0

        val plusOneCounters = driver.state.getEntity(creature)
            ?.get<CountersComponent>()?.getCount(CounterType.PLUS_ONE_PLUS_ONE) ?: 0
        plusOneCounters shouldBe damageToDeal

        val counterEventPresent = driver.events.filterIsInstance<CountersAddedEvent>().any { event ->
            event.entityId == creature &&
                event.counterType == CounterType.PLUS_ONE_PLUS_ONE.name &&
                event.amount == damageToDeal
        }
        counterEventPresent shouldBe true
    }

    test("no state-based action destroys the creature because damage was replaced not dealt") {
        val driver = createDriver()
        driver.initMirrorMatch(
            deck = Deck.of("Forest" to 20, "Mountain" to 20),
            startingLife = 20
        )

        val activePlayer = driver.activePlayer!!
        val opponent = driver.getOpponent(activePlayer)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        val fragilePowerful = CardDefinition.creature(
            name = "Fragile But Protected",
            manaCost = ManaCost.parse("{G}"),
            subtypes = setOf(Subtype("Beast")),
            power = 1,
            toughness = 1,
            oracleText = "If damage would be dealt to this creature, prevent that damage and put that many +1/+1 counters on it instead."
        )
        driver.registerCards(listOf(fragilePowerful))
        val creature = driver.putCreatureOnBattlefield(activePlayer, "Fragile But Protected")
        driver.replaceState(
            driver.state.updateEntity(creature) { container ->
                container.with(
                    ReplacementEffectSourceComponent(
                        listOf(
                            PreventDamage(
                                amount = null,
                                appliesTo = DamageEvent(
                                    recipient = RecipientFilter.Self,
                                    damageType = DamageType.Any
                                )
                            )
                        )
                    )
                )
            }
        )

        driver.giveMana(opponent, Color.RED, 1)
        val bolt = driver.putCardInHand(opponent, "Lightning Bolt")
        driver.passPriority(activePlayer)
        driver.castSpellWithTargets(
            opponent,
            bolt,
            listOf(ChosenTarget.Permanent(creature))
        )
        driver.bothPass()

        driver.findPermanent(activePlayer, "Fragile But Protected") shouldBe creature

        val plusOneCounters = driver.state.getEntity(creature)
            ?.get<CountersComponent>()?.getCount(CounterType.PLUS_ONE_PLUS_ONE) ?: 0
        plusOneCounters shouldBe damageToDeal
    }
})
