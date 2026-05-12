package com.wingedsheep.sdk.scripting.effects

import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.text.TextReplacer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Connive: draw a card, then discard a card; if the discarded card is a nonland,
 * put a +1/+1 counter on [source].
 *
 * @property source The permanent that receives the +1/+1 counter when a nonland is discarded.
 */
@SerialName("Connive")
@Serializable
data class ConniveEffect(
    val source: EffectTarget = EffectTarget.Self
) : Effect {
    override val description: String =
        "Connive — draw a card, then discard a card. If you discarded a nonland card, put a +1/+1 counter on this permanent."

    override fun applyTextReplacement(replacer: TextReplacer): Effect = this
}
