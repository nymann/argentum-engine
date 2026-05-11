package com.wingedsheep.engine.handlers.abilitywords

import com.wingedsheep.sdk.core.Keyword

// Nosh is a pure flavor marker — ability word with no rules meaning.
// The engine recognises the Keyword.NOSH prefix and passes through to
// the underlying ability's normal resolution without modification.
val NoshAbilityWord: Keyword = Keyword.NOSH
