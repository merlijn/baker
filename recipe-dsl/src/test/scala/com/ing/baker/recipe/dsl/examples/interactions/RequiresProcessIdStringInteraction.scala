package com.ing.baker.recipe.dsl.examples.interactions;

trait RequiresProcessIdStringInteraction {
    def apply(processId: String, initialIngredient: String): String
}
