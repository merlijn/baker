package com.ing.baker.il.recipe

import com.ing.baker.il.recipe.IngredientDescriptor

/**
  * Describes an event.
  *
  * @param name The name of an event.
  * @param ingredients The ingredients the event produces.
  */
case class EventDescriptor(name: String,
                           ingredients: Seq[IngredientDescriptor])
