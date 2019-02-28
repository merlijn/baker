package com.ing.baker.recipe.json

import com.ing.baker.recipe.common

case class Event(name: String, label: Option[String], providedIngredients: List[Ingredient]) extends common.Event
