package com.ing.baker.recipe.dsl.examples.interactions

trait FiresEventInteraction {
    def apply(initialIngredient: String):InteractionProvidedEvent
}

