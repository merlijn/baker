package com.ing.baker.recipe.json

import com.ing.baker.recipe.common
import com.ing.baker.types._

case class Ingredient(override val name: String, val schema: Schema) extends common.Ingredient {

  override val ingredientType: Type = schema.toType
}
