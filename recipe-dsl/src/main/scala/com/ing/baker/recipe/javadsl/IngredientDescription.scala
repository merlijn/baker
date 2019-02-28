package com.ing.baker.recipe.javadsl

import com.ing.baker.recipe.common
import com.ing.baker.types.Type

case class IngredientDescription(override val name: String, override val ingredientType: Type) extends common.Ingredient
