package com.ing.baker.recipe.javadsl

case class EventOutputTransformer(newEventName: String,
                                  ingredientRenames: Map[String, String])