package com.wingedsheep.mtg.sets.definitions.eoe.cards

import com.wingedsheep.sdk.dsl.Conditions
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.KeywordAbility
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Alpharael, Stonechosen
 * {3}{B}{B}
 * Legendary Creature — Human Cleric
 * 3/3
 *
 * Ward—Discard a card at random.
 * Void — Whenever Alpharael attacks, if a nonland permanent left the battlefield
 *   this turn or a spell was warped this turn, defending player loses half their life, rounded up.
 */
val AlpharaelStonechosen = card("Alpharael, Stonechosen") {
    manaCost = "{3}{B}{B}"
    colorIdentity = "B"
    typeLine = "Legendary Creature — Human Cleric"
    oracleText = "Ward—Discard a card at random.\nVoid — Whenever Alpharael attacks, if a nonland permanent left the battlefield this turn or a spell was warped this turn, defending player loses half their life, rounded up."
    power = 3
    toughness = 3

    keywordAbility(KeywordAbility.wardDiscard(random = true))

    triggeredAbility {
        trigger = Triggers.Attacks
        triggerCondition = Conditions.Void
        effect = Effects.LoseHalfLife(
            roundUp = true,
            target = EffectTarget.PlayerRef(Player.Opponent),
            lifePlayer = Player.Opponent
        )
        description = "Void — Whenever Alpharael attacks, if a nonland permanent left the battlefield this turn or a spell was warped this turn, defending player loses half their life, rounded up."
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "87"
        artist = "Kieran Yanner"
        imageUri = "https://cards.scryfall.io/normal/front/3/3/33063d26-37f7-4e35-8da2-5770dfabdc41.jpg"
    }
}
