package com.ing.baker.recipe.dsl

case class Event(name: String,
                 providedIngredients: Seq[Ingredient[_]] = Seq.empty,
                 firingLimit: Option[Int] = None) {

  def withMaxFiringLimit(limit: Int): Event = copy(firingLimit = Some(limit))

  def withoutFiringLimit(): Event = copy(firingLimit = None)
}
