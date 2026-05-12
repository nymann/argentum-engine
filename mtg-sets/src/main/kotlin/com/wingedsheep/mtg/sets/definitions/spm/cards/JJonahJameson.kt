package com.wingedsheep.mtg.sets.definitions.spm.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameEvent.AttackEvent
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.TriggerBinding
import com.wingedsheep.sdk.scripting.TriggerSpec
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.targets.TargetCreature

/**
 * J. Jonah Jameson
 * {2}{R}
 * Legendary Creature — Human Citizen
 * 2/2
 *
 * When J. Jonah Jameson enters, suspect up to one target creature an opponent controls.
 * Whenever a creature with menace attacks, create a Treasure token.
 */
val JJonahJameson = card("J. Jonah Jameson") {
    manaCost = "{2}{R}"
    colorIdentity = "R"
    typeLine = "Legendary Creature — Human Citizen"
    power = 2
    toughness = 2
    oracleText = "When J. Jonah Jameson enters, suspect up to one target creature an opponent controls.\n" +
        "Whenever a creature with menace attacks, create a Treasure token."

    triggeredAbility {
        trigger = Triggers.EntersBattlefield
        val t = target(
            "up to one target creature an opponent controls",
            TargetCreature(count = 1, optional = true, filter = TargetFilter.CreatureOpponentControls)
        )
        effect = Effects.Suspect(t)
    }

    triggeredAbility {
        trigger = TriggerSpec(
            AttackEvent(filter = GameObjectFilter.Creature.withKeyword(Keyword.MENACE)),
            TriggerBinding.ANY
        )
        effect = Effects.CreateTreasure()
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "54"
        artist = "Polar Engine"
        imageUri = "https://cards.scryfall.io/normal/front/0/e/0e8ec0d7-2f35-4280-a4f4-a80eb52e2f49.jpg?1757377845"
    }
}
