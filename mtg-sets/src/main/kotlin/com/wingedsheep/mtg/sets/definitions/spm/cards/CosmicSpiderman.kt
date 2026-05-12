package com.wingedsheep.mtg.sets.definitions.spm.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.effects.CompositeEffect
import com.wingedsheep.sdk.scripting.effects.ForEachInGroupEffect
import com.wingedsheep.sdk.scripting.effects.GrantKeywordEffect
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Cosmic Spider-Man
 * {W}{U}{B}{R}{G}
 * Legendary Creature — Spider Human Hero
 * 5/5
 * Flying, first strike, trample, lifelink, haste
 * At the beginning of combat on your turn, other Spiders you control gain flying,
 * first strike, trample, lifelink, and haste until end of turn.
 */
val CosmicSpiderman = card("Cosmic Spider-Man") {
    manaCost = "{W}{U}{B}{R}{G}"
    colorIdentity = "WUBRG"
    typeLine = "Legendary Creature — Spider Human Hero"
    power = 5
    toughness = 5
    oracleText = "Flying, first strike, trample, lifelink, haste\nAt the beginning of combat on your turn, other Spiders you control gain flying, first strike, trample, lifelink, and haste until end of turn."

    keywords(Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.TRAMPLE, Keyword.LIFELINK, Keyword.HASTE)

    triggeredAbility {
        trigger = Triggers.BeginCombat
        val otherSpiders = GroupFilter.OtherCreaturesYouControl.withSubtype("Spider")
        effect = CompositeEffect(listOf(
            ForEachInGroupEffect(otherSpiders, GrantKeywordEffect(Keyword.FLYING.name, EffectTarget.Self)),
            ForEachInGroupEffect(otherSpiders, GrantKeywordEffect(Keyword.FIRST_STRIKE.name, EffectTarget.Self)),
            ForEachInGroupEffect(otherSpiders, GrantKeywordEffect(Keyword.TRAMPLE.name, EffectTarget.Self)),
            ForEachInGroupEffect(otherSpiders, GrantKeywordEffect(Keyword.LIFELINK.name, EffectTarget.Self)),
            ForEachInGroupEffect(otherSpiders, GrantKeywordEffect(Keyword.HASTE.name, EffectTarget.Self))
        ))
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "1"
        artist = "Marvel Comics"
        imageUri = "https://cards.scryfall.io/normal/front/placeholder.jpg"
    }
}
