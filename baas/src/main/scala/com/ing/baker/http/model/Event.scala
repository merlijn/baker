package com.ing.baker.http.model

case class Event(name: String, label: Option[String], providedIngredients: List[Ingredient])
