package com.wingedsheep.mtg.sets.definitions.spm.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity

/**
 * Common Crook
 * {1}{B}
 * Creature — Human Rogue Villain
 * 2/2
 * When Common Crook dies, create a Treasure token.
 */
val CommonCrook = card("Common Crook") {
    manaCost = "{1}{B}"
    colorIdentity = "B"
    typeLine = "Creature — Human Rogue Villain"
    oracleText = "When Common Crook dies, create a Treasure token."
    power = 2
    toughness = 2

    triggeredAbility {
        trigger = Triggers.Dies
        effect = Effects.CreateTreasure()
    }

    metadata {
        rarity = Rarity.COMMON
        collectorNumber = "99"
        artist = "Unknown"
    }
}
