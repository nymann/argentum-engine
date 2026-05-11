package com.wingedsheep.engine.handlers.abilitywords

import com.wingedsheep.sdk.core.Keyword

// Share is a pure flavor marker — ability word with no rules meaning.
// The engine recognises the Keyword.SHARE prefix and passes through to
// the underlying ability's normal resolution without modification.
val ShareAbilityWord: Keyword = Keyword.SHARE
