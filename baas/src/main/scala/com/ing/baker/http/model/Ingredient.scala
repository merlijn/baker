package com.ing.baker.http.model

import com.ing.baker.types.Type

case class Ingredient(name: String, schema: Schema) {

  val ingredientType: Type = schema.toType
}
