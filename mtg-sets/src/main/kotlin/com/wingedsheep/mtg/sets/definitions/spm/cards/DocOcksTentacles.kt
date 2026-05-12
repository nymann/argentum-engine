package com.wingedsheep.mtg.sets.definitions.spm.cards

import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Filters
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameEvent.ZoneChangeEvent
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.TriggerBinding
import com.wingedsheep.sdk.scripting.TriggerSpec
import com.wingedsheep.sdk.scripting.effects.AttachEquipmentEffect
import com.wingedsheep.sdk.scripting.effects.MayEffect
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Doc Ock's Tentacles
 * {1}
 * Artifact — Equipment
 * Equipped creature gets +4/+4.
 * Whenever a creature with mana value 5 or greater enters the battlefield under your control,
 *   you may attach this Equipment to it.
 * Equip {5}
 */
val DocOcksTentacles = card("Doc Ock's Tentacles") {
    manaCost = "{1}"
    typeLine = "Artifact — Equipment"
    oracleText = "Equipped creature gets +4/+4.\nWhenever a creature with mana value 5 or greater enters the battlefield under your control, you may attach this Equipment to it.\nEquip {5}"

    staticAbility {
        effect = Effects.ModifyStats(4, 4)
        filter = Filters.EquippedCreature
    }

    triggeredAbility {
        trigger = TriggerSpec(
            event = ZoneChangeEvent(
                filter = GameObjectFilter.Creature.youControl().manaValueAtLeast(5),
                to = Zone.BATTLEFIELD
            ),
            binding = TriggerBinding.OTHER
        )
        effect = MayEffect(
            effect = AttachEquipmentEffect(target = EffectTarget.TriggeringEntity)
        )
    }

    equipAbility("{5}")

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "180"
        artist = "Marvel Entertainment"
        imageUri = ""
    }
}
