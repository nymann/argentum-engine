package com.wingedsheep.mtg.sets.definitions.spm.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity

/**
 * City Pigeon
 * {W}
 * Creature — Bird
 * 1/1
 * Flying
 * When City Pigeon leaves the battlefield, create a Food token.
 */
val CityPigeon = card("City Pigeon") {
    manaCost = "{W}"
    colorIdentity = "W"
    typeLine = "Creature — Bird"
    power = 1
    toughness = 1
    oracleText = "Flying\nWhen City Pigeon leaves the battlefield, create a Food token."

    keywords(Keyword.FLYING)

    triggeredAbility {
        trigger = Triggers.LeavesBattlefield
        effect = Effects.CreateFood()
        description = "When City Pigeon leaves the battlefield, create a Food token."
    }

    metadata {
        rarity = Rarity.COMMON
        collectorNumber = "13"
        artist = "Piotr Dura"
        imageUri = "https://cards.scryfall.io/normal/front/7/4/741b10b0-b658-4eb4-8a68-bfa8ee1c85cd.jpg?1757378044"
    }
}
