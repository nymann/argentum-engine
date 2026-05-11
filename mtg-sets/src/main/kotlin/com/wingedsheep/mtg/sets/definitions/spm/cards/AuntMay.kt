package com.wingedsheep.mtg.sets.definitions.spm.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity

/**
 * Aunt May
 * {W}
 * Legendary Creature — Human Citizen
 * 0/2
 *
 * Whenever another creature you control enters, you gain 1 life.
 * If that creature is a Spider, put a +1/+1 counter on it.
 */
val AuntMay = card("Aunt May") {
    manaCost = "{W}"
    colorIdentity = "W"
    typeLine = "Legendary Creature — Human Citizen"
    power = 0
    toughness = 2
    oracleText = "Whenever another creature you control enters, you gain 1 life. If that creature is a Spider, put a +1/+1 counter on it."

    triggeredAbility {
        trigger = Triggers.OtherCreatureEnters
        effect = Effects.GainLife(1)
    }

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "29"
        artist = "Julia Alekseeva"
        imageUri = "https://cards.scryfall.io/normal/front/7/a/7a1c6e53-f2a9-4f9c-a59c-cf51f43e31ad.jpg?1757377881"
    }
}
