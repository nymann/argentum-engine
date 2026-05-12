package com.wingedsheep.mtg.sets.definitions.spm.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity

/**
 * Scorpion, Seething Striker
 * {3}{B}
 * Legendary Creature — Scorpion Human Villain
 * 3/3
 * Deathtouch
 */
val ScorpionSeethingStriker = card("Scorpion, Seething Striker") {
    manaCost = "{3}{B}"
    colorIdentity = "B"
    typeLine = "Legendary Creature — Scorpion Human Villain"
    power = 3
    toughness = 3
    oracleText = "Deathtouch"

    keywords(Keyword.DEATHTOUCH)

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "64"
        artist = "Simon Dominic"
        imageUri = "https://cards.scryfall.io/normal/front/3/c/3c3d1b79-c203-4afa-989e-ccff47fc76f8.jpg?1757378029"
    }
}
