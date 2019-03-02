package com.ing.baker.recipe.javadsl

import com.ing.baker.recipe.javadsl
import com.ing.baker.types.mirror

import scala.language.experimental.macros
import scala.reflect.runtime.universe.TypeTag

object Event {

  def apply(ingredients: Ingredient*) : Event = macro CommonMacros.eventImpl

  def apply(name: String, ingredients: Ingredient*) : Event = new Event(name, ingredients, Some(1))

  def apply[T : TypeTag]: Event = fromClass(mirror.runtimeClass(mirror.typeOf[T]))

  def fromClass(eventClass: Class[_], maxFiringLimit: Option[Int] = None): Event = {
    val name = eventClass.getSimpleName

    val ingredients: Seq[Ingredient] = eventClass.getDeclaredFields
      .filter(field => !field.isSynthetic)
      .map(f => Ingredient(f.getName, ReflectionHelpers.parseType(f.getGenericType, s"Unsupported type for ingredient '${f.getName}' on event '${eventClass.getSimpleName}'")))
      .toSeq

    Event(name, ingredients, maxFiringLimit)
  }

  /**
    * Creates the retry exhausted event name for the interaction the class represents.
    *
    * @param interactionClass
    * @return
    */
  def retryExhaustedEventName(interactionClass: Class[_]): String =
    interactionClass.getSimpleName + javadsl.exhaustedEventAppend

  /**
    * Creates the retry exhausted event name for a interaction with this hame
    * This operation is usefull if you rename a operation
    *
    * @param interactionName the name of the interaction
    * @return
    */
  def retryExhaustedEventName(interactionName: String): String =
    interactionName + javadsl.exhaustedEventAppend
}

case class Event(name: String,
                 providedIngredients: Seq[Ingredient],
                 maxFiringLimit: Option[Int]) {

  def withMaxFiringLimit(limit: Int) = copy(maxFiringLimit = Some(limit))

  def withoutFiringLimit() = copy(maxFiringLimit = None)
}
