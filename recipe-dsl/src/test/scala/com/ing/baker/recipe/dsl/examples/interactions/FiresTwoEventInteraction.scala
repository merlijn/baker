package com.ing.baker.recipe.dsl.examples.interactions;

import com.ing.baker.recipe.dsl.examples.events.InteractionEventExample

trait FiresTwoEventInteraction {
  def apply(initialIngredient: String): InteractionEventExample
}

