package com.ing.baker.recipe.dsl

case class Event(name: String,
                 providedIngredients: Seq[Ingredient[_]] = Seq.empty,
                 maxFiringLimit: Option[Int] = None) {

  def withMaxFiringLimit(limit: Int): Event = copy(maxFiringLimit = Some(limit))

  def withoutFiringLimit(): Event = copy(maxFiringLimit = None)
}
