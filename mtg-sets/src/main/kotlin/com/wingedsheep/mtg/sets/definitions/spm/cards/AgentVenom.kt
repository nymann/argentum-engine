package com.wingedsheep.mtg.sets.definitions.spm.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity

/**
 * Agent Venom
 * {2}{B}
 * Legendary Creature — Symbiote Soldier Hero
 * 2/3
 * Flash, Menace
 */
val AgentVenom = card("Agent Venom") {
    manaCost = "{2}{B}"
    colorIdentity = "B"
    typeLine = "Legendary Creature — Symbiote Soldier Hero"
    power = 2
    toughness = 3
    oracleText = "Flash\nMenace"

    keywords(Keyword.FLASH, Keyword.MENACE)

    metadata {
        rarity = Rarity.UNCOMMON
    }
}
