package com.wingedsheep.mtg.sets.definitions.spm.cards

import com.wingedsheep.sdk.core.Counters
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.TimingRule
import com.wingedsheep.sdk.scripting.targets.TargetCreature

/**
 * Bagel and Schmear
 * {1}
 * Artifact — Food
 * Share — {W}, {T}, Sacrifice this artifact: Put a +1/+1 counter on up to one target creature,
 *   then draw a card. Activate only as a sorcery.
 * Nosh — {2}, {T}, Sacrifice this artifact: You gain 3 life and draw a card.
 */
val BagelAndSchmear = card("Bagel and Schmear") {
    manaCost = "{1}"
    colorIdentity = "W"
    typeLine = "Artifact — Food"
    oracleText = "Share — {W}, {T}, Sacrifice this artifact: Put a +1/+1 counter on up to one target creature, then draw a card. Activate only as a sorcery.\nNosh — {2}, {T}, Sacrifice this artifact: You gain 3 life and draw a card."

    activatedAbility {
        cost = Costs.Composite(Costs.Mana("{W}"), Costs.Tap, Costs.SacrificeSelf)
        val creature = target("up to one target creature", TargetCreature(optional = true))
        effect = Effects.Composite(
            Effects.AddCounters(Counters.PLUS_ONE_PLUS_ONE, 1, creature),
            Effects.DrawCards(1)
        )
        timing = TimingRule.SorcerySpeed
    }

    activatedAbility {
        cost = Costs.Composite(Costs.Mana("{2}"), Costs.Tap, Costs.SacrificeSelf)
        effect = Effects.Composite(
            Effects.GainLife(3),
            Effects.DrawCards(1)
        )
    }

    metadata {
        rarity = Rarity.COMMON
        collectorNumber = "26"
        artist = "Marta Nael"
        imageUri = "https://cards.scryfall.io/normal/front/b/a/ba6b1827-db69-4b07-9f9c-0cd30a3c13a9.jpg?1757376788"
    }
}
