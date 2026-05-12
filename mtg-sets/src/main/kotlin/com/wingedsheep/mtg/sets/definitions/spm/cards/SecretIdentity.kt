package com.wingedsheep.mtg.sets.definitions.spm.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Targets
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.Duration
import com.wingedsheep.sdk.scripting.effects.BecomeCreatureEffect
import com.wingedsheep.sdk.scripting.effects.ModalEffect
import com.wingedsheep.sdk.scripting.effects.Mode
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Secret Identity
 * {U}
 * Instant
 *
 * Choose one —
 * • Reveal — Draw a card.
 * • Conceal — Until end of turn, target creature becomes a Citizen with base power
 *   and toughness 1/1 and gains hexproof.
 */
val SecretIdentity = card("Secret Identity") {
    manaCost = "{U}"
    colorIdentity = "U"
    typeLine = "Instant"
    oracleText = "Choose one —\n• Reveal — Draw a card.\n• Conceal — Until end of turn, target creature becomes a Citizen with base power and toughness 1/1 and gains hexproof."

    spell {
        effect = ModalEffect.chooseOne(
            Mode.noTarget(
                Effects.DrawCards(1),
                "Reveal — Draw a card"
            ),
            Mode.withTarget(
                BecomeCreatureEffect(
                    target = EffectTarget.ContextTarget(0),
                    power = 1,
                    toughness = 1,
                    keywords = setOf(Keyword.HEXPROOF),
                    creatureTypes = setOf("Citizen"),
                    duration = Duration.EndOfTurn
                ),
                Targets.Creature,
                "Conceal — Until end of turn, target creature becomes a Citizen with base power and toughness 1/1 and gains hexproof"
            )
        )
    }

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "TBD"
        artist = "TBD"
        imageUri = ""
    }
}
