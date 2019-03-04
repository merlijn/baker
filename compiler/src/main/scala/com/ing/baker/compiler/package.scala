package com.ing.baker

import com.ing.baker.il.petrinet._
import com.ing.baker.il.{EventDescriptor, InteractionFailureStrategy, _}
import com.ing.baker.recipe.javadsl
import com.ing.baker.recipe.javadsl.Interaction
import com.ing.baker.types._

package object compiler {

  def parseDSLEvent(event: javadsl.Event): EventDescriptor =
    EventDescriptor(event.name, event.providedIngredients.map(e => IngredientDescriptor(e.name, e.ingredientType)))

  def parseDSLInteraction(interactionDescriptor: Interaction,
                          defaultFailureStrategy: javadsl.InteractionFailureStrategy,
                          allIngredientNames: Set[String]): InteractionTransition = {

    //Replace ProcessId to ProcessIdName tag as know in compiledRecipe-
    //Replace ingredient tags with overridden tags
    val inputFields: Seq[(String, Type)] = interactionDescriptor.input
      .map { ingredient =>
        if (ingredient.name == javadsl.processIdName) il.processIdName -> ingredient.ingredientType
        else interactionDescriptor.renamedInputIngredients.getOrElse(ingredient.name, ingredient.name) -> ingredient.ingredientType
      }

    val originalEvents: Seq[EventDescriptor] = interactionDescriptor.output.map(e => parseDSLEvent(e))

    val eventOutputTransformers: Map[String, EventOutputTransformer] = interactionDescriptor.eventOutputTransformers.map {
      case (event, transformer) => event.name -> EventOutputTransformer(transformer.newEventName, transformer.ingredientRenames) }

    val predefinedIngredients: Map[String, Value] =
      inputFields.flatMap {
        // in case the ingredient is optional and not provided anywhere it is predefined as null (None, Optional.empty())
        case (name, types.OptionType(_)) if !allIngredientNames.contains(name) => Seq(name -> NullValue)
        case _ => Seq.empty
      }.toMap ++ interactionDescriptor.predefinedIngredients

    val (failureStrategy: InteractionFailureStrategy, exhaustedRetryEvent: Option[EventDescriptor]) = {
      interactionDescriptor.failureStrategy.getOrElse[javadsl.InteractionFailureStrategy](defaultFailureStrategy) match {
        case javadsl.InteractionFailureStrategy.RetryWithIncrementalBackoff(initialTimeout, backoffFactor, maximumRetries, maxTimeBetweenRetries, fireRetryExhaustedEvent) =>
          val exhaustedRetryEvent: Option[EventDescriptor] = fireRetryExhaustedEvent match {
            case Some(None)            => Some(EventDescriptor(interactionDescriptor.name + exhaustedEventAppend, Seq.empty))
            case Some(Some(eventName)) => Some(EventDescriptor(eventName, Seq.empty))
            case None                  => None
          }

          (il.InteractionFailureStrategy.RetryWithIncrementalBackoff(initialTimeout, backoffFactor, maximumRetries, maxTimeBetweenRetries, exhaustedRetryEvent), exhaustedRetryEvent)
        case javadsl.InteractionFailureStrategy.BlockInteraction() => (

          il.InteractionFailureStrategy.BlockInteraction, None)
        case javadsl.InteractionFailureStrategy.FireEventAfterFailure(eventNameOption) =>
          val eventName = eventNameOption.getOrElse(interactionDescriptor.name + exhaustedEventAppend)
          val exhaustedRetryEvent: EventDescriptor = EventDescriptor(eventName, Seq.empty)

          (il.InteractionFailureStrategy.FireEventAfterFailure(exhaustedRetryEvent), Some(exhaustedRetryEvent))
        case _ =>

          (il.InteractionFailureStrategy.BlockInteraction, None)
      }
    }

    InteractionTransition(
      originalEvents = originalEvents ++ exhaustedRetryEvent,
      requiredIngredients = inputFields.map { case (name, ingredientType) => IngredientDescriptor(name, ingredientType) },
      name = interactionDescriptor.name,
      originalName = interactionDescriptor.originalName.getOrElse(interactionDescriptor.name),
      predefinedIngredients = predefinedIngredients,
      maximumExecutionCount = interactionDescriptor.maximumExecutionCount,
      failureStrategy = failureStrategy,
      eventOutputTransformers = eventOutputTransformers )
  }
}
