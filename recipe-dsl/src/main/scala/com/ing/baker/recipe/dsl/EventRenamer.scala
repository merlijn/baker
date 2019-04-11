package com.ing.baker.recipe.dsl

case class EventRenamer(newEventName: String,
                        ingredientRenames: Map[String, String])