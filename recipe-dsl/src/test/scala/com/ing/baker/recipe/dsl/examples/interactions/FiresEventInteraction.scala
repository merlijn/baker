package com.ing.baker.recipe.dsl.examples.interactions

import com.ing.baker.recipe.dsl.examples.events.InteractionProvidedEvent

trait FiresEventInteraction {
    def apply(initialIngredient: String): InteractionProvidedEvent
}

