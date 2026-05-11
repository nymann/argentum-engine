package com.wingedsheep.mtg.sets.definitions.spm.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Targets
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.effects.ForEachTargetEffect
import com.wingedsheep.sdk.scripting.effects.Mode
import com.wingedsheep.sdk.scripting.effects.ModalEffect
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Amazing Acrobatics
 * {1}{U}{U}
 * Instant
 * Choose one or both —
 * • Counter target spell.
 * • Tap up to two target creatures.
 */
val AmazingAcrobatics = card("Amazing Acrobatics") {
    manaCost = "{1}{U}{U}"
    colorIdentity = "U"
    typeLine = "Instant"
    oracleText = "Choose one or both —\n• Counter target spell.\n• Tap up to two target creatures."

    spell {
        effect = ModalEffect(
            modes = listOf(
                Mode.withTarget(
                    effect = Effects.CounterSpell(),
                    target = Targets.Spell,
                    description = "Counter target spell."
                ),
                Mode.withTarget(
                    effect = ForEachTargetEffect(listOf(Effects.Tap(EffectTarget.ContextTarget(0)))),
                    target = Targets.UpToCreatures(2),
                    description = "Tap up to two target creatures."
                )
            ),
            chooseCount = 2,
            minChooseCount = 1
        )
    }

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "1"
        artist = "Unknown"
        imageUri = ""
    }
}
