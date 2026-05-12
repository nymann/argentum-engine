package com.wingedsheep.mtg.sets.definitions.spm.cards

import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.effects.CardDestination
import com.wingedsheep.sdk.scripting.effects.CardSource
import com.wingedsheep.sdk.scripting.effects.CompositeEffect
import com.wingedsheep.sdk.scripting.effects.GatherCardsEffect
import com.wingedsheep.sdk.scripting.effects.MoveCollectionEffect
import com.wingedsheep.sdk.scripting.effects.SelectFromCollectionEffect
import com.wingedsheep.sdk.scripting.effects.SelectionMode
import com.wingedsheep.sdk.scripting.effects.SelectionRestriction
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.values.DynamicAmount

/**
 * Behold the Sinister Six!
 * {6}{B}
 * Sorcery
 * Return up to six target creature cards with different names from your graveyard to the battlefield.
 */
val BeholdTheSinisterSix = card("Behold the Sinister Six!") {
    manaCost = "{6}{B}"
    colorIdentity = "B"
    typeLine = "Sorcery"
    oracleText = "Return up to six target creature cards with different names from your graveyard to the battlefield."

    spell {
        effect = CompositeEffect(
            listOf(
                GatherCardsEffect(
                    source = CardSource.FromZone(Zone.GRAVEYARD, Player.You, GameObjectFilter.Creature),
                    storeAs = "sinisterTargets"
                ),
                SelectFromCollectionEffect(
                    from = "sinisterTargets",
                    selection = SelectionMode.ChooseUpTo(DynamicAmount.Fixed(6)),
                    storeSelected = "sinisterChosen",
                    restrictions = listOf(SelectionRestriction.OnePerCardName),
                    prompt = "Choose up to six creature cards with different names from your graveyard"
                ),
                MoveCollectionEffect(
                    from = "sinisterChosen",
                    destination = CardDestination.ToZone(Zone.BATTLEFIELD)
                )
            )
        )
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "15"
        artist = "Filipe Pagliuso"
        imageUri = "https://cards.scryfall.io/normal/front/b/e/be3a4c9b-d0f3-4f75-9c0e-b7e2b9e2a132.jpg?1752946985"
    }
}
