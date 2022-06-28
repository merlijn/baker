package com.ing.baker.il.recipe

import com.ing.baker.il.recipe.{EventDescriptor, IngredientDescriptor}

case class EventOutputTransformer(newEventName: String, ingredientRenames: Map[String, String]) {
  def apply(event: EventDescriptor): EventDescriptor =
    EventDescriptor(
      newEventName,
      event.ingredients.map(i => IngredientDescriptor(ingredientRenames.getOrElse(i.name, i.name))))
}
