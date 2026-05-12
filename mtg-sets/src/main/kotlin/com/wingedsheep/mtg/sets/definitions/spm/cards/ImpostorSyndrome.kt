package com.wingedsheep.mtg.sets.definitions.spm.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Impostor Syndrome
 * {4}{U}{U}
 * Enchantment
 * Whenever a nontoken creature you control deals combat damage to a player, create a token
 * that's a copy of that creature, except it's not legendary.
 */
val ImpostorSyndrome = card("Impostor Syndrome") {
    manaCost = "{4}{U}{U}"
    colorIdentity = "U"
    typeLine = "Enchantment"
    oracleText = "Whenever a nontoken creature you control deals combat damage to a player, create a token that's a copy of that creature, except it's not legendary."

    triggeredAbility {
        trigger = Triggers.NontokenCreatureYouControlDealsCombatDamageToPlayer
        effect = Effects.CreateTokenCopyOfTarget(
            target = EffectTarget.TriggeringEntity,
            removeLegendary = true
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "54"
        artist = "Mike McKone"
        imageUri = "https://cards.scryfall.io/normal/front/4/5/4518be73-2571-4eef-b7a0-3eac5bc3a8c3.jpg?1757376788"
    }
}
