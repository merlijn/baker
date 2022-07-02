package com.ing.baker.compiler

import com.ing.baker.graph.api.MultiSet
import com.ing.baker.recipe.dsl.{Event, Ingredient, Interaction, Recipe}

import scala.collection.mutable

object Assertions {
  def assertNoDuplicateElementsExist[T](compareIdentifier: T => Any, elements: Seq[T]) = {

    val mset: MultiSet[T] = MultiSet.copyOff(elements)

    mset.foreach {
      case (e, count) if count > 1 => throw new IllegalStateException(s"Duplicate element: $e")
      case (e, _)                  => (mset - e).keys.find(c => compareIdentifier(c) == compareIdentifier(e)).foreach { c => throw new IllegalStateException(s"Duplicate identifiers found: ${e.getClass.getSimpleName}:$e and ${c.getClass.getSimpleName}:$c") }
    }
  }

  def assertValidNames[T](nameFunc: T => String, list: Iterable[T], typeName: String) = 
    list.map(nameFunc)
        .filter(name => name == null || name.isEmpty)
        .foreach { _ => throw new IllegalArgumentException(s"$typeName with a null or empty name found")
  }

  // TODO move to better place as extension method
  def toOpt[T](bool: Boolean)(e: T): Option[T] = {
    if (bool) Some(e)
    else      None
  }
  
  def assertNonEmptyRecipe(recipe: Recipe): Iterable[String] = {
    
    val noEvents = toOpt(recipe.sensoryEvents.isEmpty)("No sensory events found.")
    val noInteractions = toOpt(recipe.interactions.size == 0)("No interactions found.")
    
    noEvents ++ noInteractions
  }

  def preCompileAssertions(recipe: Recipe): Unit = {
    
    assertValidNames[Recipe](_.name, Seq(recipe), "Recipe")
    assertValidNames[Interaction](_.name, recipe.interactions, "Interaction")
    assertValidNames[Event](_.name, recipe.sensoryEvents, "Event")
    val allIngredients: Iterable[Ingredient[_]] = recipe.sensoryEvents.flatMap(_.providedIngredients) ++ recipe.interactions.flatMap(_.input)
    
    assertValidNames[Ingredient[_]](_.name, allIngredients, "Ingredient")
    assertNoDuplicateElementsExist[Interaction](_.name, recipe.interactions)
    assertNoDuplicateElementsExist[Event](_.name, recipe.sensoryEvents)
    
    // TODO this does not assert anything
    assertNonEmptyRecipe(recipe)
  }
}
