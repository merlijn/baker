package com.ing.baker.recipe.dsl

import java.lang.reflect.Method

import com.ing.baker.recipe.dsl
import org.reflections.Reflections

import scala.annotation.varargs
import scala.collection.JavaConverters._

case class Interaction(
      name: String,
      input: Seq[Ingredient[_]],
      output: Seq[Event],
      originalName: Option[String] = None,
      requiredEvents: Set[String] = Set.empty,
      requiredOneOfEvents: Set[Set[String]] = Set.empty,
      predefinedIngredients: Map[String, Any] = Map.empty,
      renamedInputIngredients: Map[String, String] = Map.empty,
      maximumExecutionCount: Option[Int] = None,
      failureStrategy: Option[InteractionFailureStrategy] = None,
      eventRenames: Map[String, EventRenamer] = Map.empty) {

  /**
    * The retry exhausted event name
    *
    * @return
    */
  def retryExhaustedEventName: String = name + Constants.exhaustedEventAppend

  /**
    * This sets a requirement for this interaction that some specific events needs to have been fired before it can execute.
    *
    * @param eventClasses the classes of the events.
    * @return
    */
  def withRequiredEvents(eventClasses: Event*): Interaction =
    copy(requiredEvents = requiredEvents ++ eventClasses.map(_.name))

  /**
    * This sets a requirement for this interaction that one of the given events needs to have been fired before it can execute.
    *
    * @param eventClasses the classes of the events.
    * @return
    */
  def withRequiredOneOfEvents(eventClasses: Event*): Interaction = {
    if (eventClasses.nonEmpty && eventClasses.size < 2)
      throw new IllegalArgumentException("At least 2 events should be provided as 'requiredOneOfEvents'")

    val newRequired: Set[Set[String]] = requiredOneOfEvents + eventClasses.map(_.name).toSet

    copy(requiredOneOfEvents = newRequired)
  }

  /**
    * This sets a input ingredient to a set value. In this case the ingredient wont be taken from the runtime recipe.
    *
    * @param ingredientName  the name of the ingredient
    * @param ingredientValue the value of the ingredient
    * @return
    */
  def withPredefinedIngredient(ingredientName: String,
                               ingredientValue: AnyRef): Interaction =
    addPredefinedIngredient(Map(ingredientName -> ingredientValue))

  /**
    * This sets input ingredients to set values. In this case the ingredients wont be taken from the runtime recipe.
    *
    * @param newPredefinedIngredients The map containing ingredientName and ingredientValue for ingredients you want to set
    * @return
    */
  def withPredefinedIngredients(newPredefinedIngredients: java.util.Map[String, AnyRef]): Interaction =
    addPredefinedIngredient(newPredefinedIngredients.asScala.toMap)

  private def addPredefinedIngredient(params: Map[String, Any]): Interaction =
    copy(predefinedIngredients = predefinedIngredients ++ params)

  /**
    * This renames a input ingredient
    *
    * @param name the name of the input ingredient you want to rename
    * @param toName the new name for the ouput ingredient
    * @return
    */
  def renameRequiredIngredient(name: String, toName: String): Interaction =
    copy(renamedInputIngredients = renamedInputIngredients + (name -> toName))

  /**
    * This renames the given input ingredients
    *
    * @param newOverriddenIngredients a map containing old and new names for input ingredients
    * @return new InteractionDescriptor with new ingredient names
    */
  def renameRequiredIngredients(newOverriddenIngredients: java.util.Map[String, String]): Interaction = {
    copy(renamedInputIngredients = renamedInputIngredients ++ newOverriddenIngredients.asScala.toMap)
  }

  def withFailureStrategy(interactionFailureStrategy: InteractionFailureStrategy): Interaction = {
    this.copy(failureStrategy = Some(interactionFailureStrategy))
  }

  /**
    * Sets the maximum amount of times this interaction can be fired.
    *
    * @param times maximum amount of times this interaction can be fired
    * @return
    */
  def withMaximumInteractionCount(times: Int): Interaction =
    this.copy(maximumExecutionCount = Some(times))


  def withName(newName: String): Interaction = copy(name = newName, originalName = Some(originalName.getOrElse(name)))

  def withRequiredEvents(events: Set[Event]): Interaction = copy(requiredEvents = requiredEvents ++ events.map(_.name))

  def withRequiredOneOfEvents(newRequiredOneOfEvents: Set[Event]): Interaction = {
    if (newRequiredOneOfEvents.nonEmpty && newRequiredOneOfEvents.size < 2)
      throw new IllegalArgumentException("At least 2 events should be provided as 'requiredOneOfEvents'")

    val newRequired: Set[Set[String]] = requiredOneOfEvents + newRequiredOneOfEvents.map(_.name)

    copy(requiredOneOfEvents = newRequired)
  }

  def withPredefinedIngredients(values: (String, Any)*): Interaction =
    withPredefinedIngredients(values.toMap)

  def withPredefinedIngredients(data: Map[String, Any]): Interaction = 
    copy(predefinedIngredients = predefinedIngredients ++ data)

  def withOverriddenIngredientName(oldIngredient: String,
                                   newIngredient: String): Interaction =
    copy(renamedInputIngredients = renamedInputIngredients + (oldIngredient -> newIngredient))

  def withEventOutputTransformer(event: Event, ingredientRenames: Map[String, String]): Interaction =
    copy(eventRenames = eventRenames + (event.name -> EventRenamer(event.name, ingredientRenames)))

  def withEventOutputTransformer(event: Event, newEventName: String, ingredientRenames: Map[String, String]): Interaction =
    copy(eventRenames = eventRenames + (event.name -> EventRenamer(newEventName, ingredientRenames)))
}