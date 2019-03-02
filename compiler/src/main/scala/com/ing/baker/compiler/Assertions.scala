package com.ing.baker.compiler

import com.ing.baker.petrinet.api.MultiSet
import com.ing.baker.recipe.javadsl.{Event, Ingredient, Interaction, Recipe}

import scala.collection.mutable

object Assertions {
  private def assertNoDuplicateElementsExist[T](compareIdentifier: T => Any, elements: Seq[T]) = {

    val mset: MultiSet[T] = MultiSet.copyOff(elements)

    mset.foreach {
      case (e, count) if count > 1 => throw new IllegalStateException(s"Duplicate element: $e")
      case (e, _)                  => (mset - e).keys.find(c => compareIdentifier(c) == compareIdentifier(e)).foreach { c => throw new IllegalStateException(s"Duplicate identifiers found: ${e.getClass.getSimpleName}:$e and ${c.getClass.getSimpleName}:$c") }
    }
  }

  private def assertValidNames[T](nameFunc: T => String, list: Iterable[T], typeName: String) = list.map(nameFunc).filter(name => name == null || name.isEmpty).foreach { _ =>
    throw new IllegalArgumentException(s"$typeName with a null or empty name found")
  }

  private def assertNonEmptyRecipe(recipe: Recipe): Seq[String] = {
    val errors = mutable.MutableList.empty[String]
    if (recipe.sensoryEvents.isEmpty)
      errors += "No sensory events found."
    if (recipe.interactions.size == 0)
      errors += "No interactions found."
    errors
  }

  def preCompileAssertions(recipe: Recipe): Seq[String] = {
    assertValidNames[Recipe](_.name, Seq(recipe), "Recipe")
    assertValidNames[Interaction](_.name, recipe.interactions, "Interaction")
    assertValidNames[Event](_.name, recipe.sensoryEvents, "Event")
    val allIngredients = recipe.sensoryEvents.flatMap(_.providedIngredients) ++ recipe.interactions.flatMap(_.input)
    assertValidNames[Ingredient](_.name, allIngredients, "Ingredient")
    assertNoDuplicateElementsExist[Interaction](_.name, recipe.interactions)
    assertNoDuplicateElementsExist[Event](_.name, recipe.sensoryEvents)
    assertNonEmptyRecipe(recipe)
  }
}
