package com.wingedsheep.mtg.sets.definitions.spm.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Targets
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.EntersWithCounters
import com.wingedsheep.sdk.scripting.events.CounterTypeFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Selfless Police Captain
 * {1}{W}
 * Creature — Human Soldier
 * 1/1
 * Selfless Police Captain enters with a +1/+1 counter on it.
 * When Selfless Police Captain leaves the battlefield, move its +1/+1 counters
 *   onto target creature you control.
 */
val SelflessPoliceCaptain = card("Selfless Police Captain") {
    manaCost = "{1}{W}"
    colorIdentity = "W"
    typeLine = "Creature — Human Soldier"
    power = 1
    toughness = 1
    oracleText = "Selfless Police Captain enters with a +1/+1 counter on it.\nWhen Selfless Police Captain leaves the battlefield, move its +1/+1 counters onto target creature you control."

    replacementEffect(EntersWithCounters(
        counterType = CounterTypeFilter.PlusOnePlusOne,
        count = 1,
        selfOnly = true
    ))

    triggeredAbility {
        trigger = Triggers.LeavesBattlefield
        target = Targets.CreatureYouControl
        effect = Effects.MoveAllLastKnownCounters(EffectTarget.ContextTarget(0))
    }

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "17"
        artist = "Kari Christensen"
    }
}
