package com.wingedsheep.mtg.sets.definitions.spm.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Filters
import com.wingedsheep.sdk.dsl.Targets
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity

/**
 * Biorganic Carapace
 * {2}{W}{U}
 * Artifact — Equipment
 * When this Equipment enters, attach it to target creature you control.
 * Equipped creature gets +2/+2.
 * Equip {2}
 */
val BiorganicCarapace = card("Biorganic Carapace") {
    manaCost = "{2}{W}{U}"
    colorIdentity = "WU"
    typeLine = "Artifact — Equipment"
    oracleText = "When this Equipment enters, attach it to target creature you control.\nEquipped creature gets +2/+2.\nEquip {2}"

    triggeredAbility {
        trigger = Triggers.EntersBattlefield
        val creature = target("target creature you control", Targets.CreatureYouControl)
        effect = Effects.AttachEquipment(creature)
    }

    staticAbility {
        effect = Effects.ModifyStats(2, 2)
        filter = Filters.EquippedCreature
    }

    equipAbility("{2}")

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "1"
        artist = "Unknown"
        imageUri = "https://cards.scryfall.io/normal/front/placeholder.jpg"
    }
}
