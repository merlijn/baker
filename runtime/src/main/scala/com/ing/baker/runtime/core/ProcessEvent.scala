package com.ing.baker.runtime.core

import com.ing.baker.il.recipe.EventDescriptor

object ProcessEvent {

  /**
    * Parses a POJO object into a ProcessEvent if possible.
    *
    * @param event The event object.
    * @return
    */
  def of(event: Any): ProcessEvent = {
    event match {
      case runtimeEvent: ProcessEvent => runtimeEvent
      case obj                        => null // TODO implement
    }
  }

  def apply(name: String, providedIngredients: Seq[(String, Any)]): ProcessEvent =
    ProcessEvent(name, providedIngredients.toMap)
}

case class ProcessEvent(name: String,
                        providedIngredients: Map[String, Any]) {

  /**
    * This checks if the runtime event is an instance of a event type.
    *
    * @param eventType
    * @return
    */
  def isInstanceOfEventType(eventType: EventDescriptor): Boolean = validateEvent(eventType).isEmpty

  /**
    *
    * Validates the runtime event against a event type and returns a sequence
    * of validation errors.
    *
    * @param eventType The event type to validate against.
    * @return
    */
  def validateEvent(eventType: EventDescriptor): Seq[String] = {

    if (eventType.name != name)
      Seq(s"Provided event with name '$name' does not match expected name '${eventType.name}'")
    else
      // we check all the required ingredient types, additional ones are ignored
      eventType.ingredients.flatMap { ingredient =>
        providedIngredients.get(ingredient.name) match {
          case None        =>
            Seq(s"no value was provided for ingredient '${ingredient.name}'")
          // we can only check the class since the type parameters are available on objects
          case Some(null) =>
            Seq(s"null is not allowed ingredients")
          case Some(value) =>
            Seq.empty
//            value.validate(ingredient.`type`).map(
//              reason => s"ingredient '${ingredient.name}' has an incorrect type:\n$reason"
//            ).toSeq
          case _ =>
            Seq.empty
        }
    }
  }
}