package com.ing.baker.recipe.dsl

case class EventOutputTransformer(newEventName: String,
                                  ingredientRenames: Map[String, String])