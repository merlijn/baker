package com.ing.baker.runtime.core.implementations

import com.ing.baker.recipe.dsl.examples.TestRecipe
import com.ing.baker.recipe.dsl.examples.TestRecipe.InteractionOneSuccessful

class InteractionOneInterfaceImplementation() extends TestRecipe.InteractionOne {
  override def apply(processId: String, initialIngredient: String): InteractionOneSuccessful = InteractionOneSuccessful("")
}
