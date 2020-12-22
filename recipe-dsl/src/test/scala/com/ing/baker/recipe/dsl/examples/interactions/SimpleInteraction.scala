package com.ing.baker.recipe.dsl.examples.interactions

import com.ing.baker.recipe.dsl.examples.events.InitialIngredientEvent

trait SimpleInteraction {
    
    def apply(initialIngredient: String): InitialIngredientEvent
}
