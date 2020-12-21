package com.ing.baker.recipe.dsl.examples.interactions;

trait SimpleInteraction {
    
    def apply(initialIngredient: String): InitialIngredientEvent
}
