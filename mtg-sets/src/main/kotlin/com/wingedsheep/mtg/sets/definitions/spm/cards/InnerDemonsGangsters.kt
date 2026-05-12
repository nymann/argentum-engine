package com.wingedsheep.mtg.sets.definitions.spm.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.TimingRule
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Inner Demons Gangsters
 * {3}{B}
 * Creature — Human Rogue Villain
 * 3/4
 *
 * {B}, Discard a card: This creature gets +1/+0 and gains menace until end of turn.
 * Activate only as a sorcery.
 */
val InnerDemonsGangsters = card("Inner Demons Gangsters") {
    manaCost = "{3}{B}"
    colorIdentity = "B"
    typeLine = "Creature — Human Rogue Villain"
    power = 3
    toughness = 4
    oracleText = "{B}, Discard a card: This creature gets +1/+0 and gains menace until end of turn. Activate only as a sorcery."

    activatedAbility {
        cost = Costs.Composite(Costs.Mana("{B}"), Costs.DiscardCard)
        effect = Effects.ModifyStats(1, 0, EffectTarget.Self)
            .then(Effects.GrantKeyword(Keyword.MENACE, EffectTarget.Self))
        timing = TimingRule.SorcerySpeed
    }

    metadata {
        rarity = Rarity.COMMON
        collectorNumber = "96"
        artist = "Lie Setiawan"
        imageUri = "https://cards.scryfall.io/normal/front/b/4/b4d5b03f-4cb4-471f-8f67-23c6dc54c0d8.jpg?1748706025"
    }
}
