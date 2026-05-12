package com.wingedsheep.sdk.scripting.effects

import com.wingedsheep.sdk.scripting.text.TextReplacer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Return the source card and one target land card from the controller's graveyard
 * to the battlefield tapped.
 *
 * The target land is identified by the first entry in EffectContext.targets.
 * Non-land cards in the graveyard are never legal targets.
 */
@SerialName("ReturnSelfAndTargetLandCardFromGraveyardToBattlefieldTapped")
@Serializable
data object ReturnSelfAndTargetLandCardFromGraveyardToBattlefieldTappedEffect : Effect {
    override val description: String =
        "Return this card and target land card from your graveyard to the battlefield tapped"

    override fun applyTextReplacement(replacer: TextReplacer): Effect = this
}
