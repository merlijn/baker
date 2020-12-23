package com.ing.baker

import com.ing.baker.il.petrinet._
import com.ing.baker.il.{EventDescriptor, InteractionFailureStrategy, _}
import com.ing.baker.recipe.dsl
import com.ing.baker.recipe.dsl.Interaction

package object compiler {

  def parseDSLEvent(event: dsl.Event): EventDescriptor =
    EventDescriptor(event.name, event.providedIngredients.map(e => IngredientDescriptor(e.name)))

  def parseDSLInteraction(interactionDescriptor: Interaction,
                          defaultFailureStrategy: dsl.InteractionFailureStrategy,
                          allIngredientNames: Set[String]): InteractionTransition = {

    //Replace ProcessId to ProcessIdName tag as know in compiledRecipe-
    //Replace ingredient tags with overridden tags
    val inputFields: Seq[String] = interactionDescriptor.input.map { ingredient =>
      //  TODO inject process ids in some other way
      //  if (ingredient.name == dsl.Constants.processIdName) il.processIdName -> ingredient.ingredientType
        interactionDescriptor.renamedInputIngredients.getOrElse(ingredient.name, ingredient.name)
      }

    val originalEvents: Seq[EventDescriptor] = interactionDescriptor.output.map(e => parseDSLEvent(e))

    val eventOutputTransformers: Map[String, EventOutputTransformer] = interactionDescriptor.eventRenames.map {
      case (event, transformer) => event -> EventOutputTransformer(transformer.newEventName, transformer.ingredientRenames) }

    // TODO provide None values at compile time in dsl
    val predefinedIngredients: Map[String, Any] = interactionDescriptor.predefinedIngredients
    // in case the ingredient is optional and not provided anywhere it is predefined as null (None, Optional.empty())
    // case (name, types.OptionType(_)) if !allIngredientNames.contains(name) => Seq(name -> NullValue)

    val (failureStrategy: InteractionFailureStrategy, exhaustedRetryEvent: Option[EventDescriptor]) = {
      interactionDescriptor.failureStrategy.getOrElse[dsl.InteractionFailureStrategy](defaultFailureStrategy) match {
        case dsl.InteractionFailureStrategy.RetryWithIncrementalBackoff(initialTimeout, backoffFactor, maximumRetries, maxTimeBetweenRetries, fireRetryExhaustedEvent) =>
          val exhaustedRetryEvent: Option[EventDescriptor] = fireRetryExhaustedEvent match {
            case Some(None)            => Some(EventDescriptor(interactionDescriptor.name + exhaustedEventAppend, Seq.empty))
            case Some(Some(eventName)) => Some(EventDescriptor(eventName, Seq.empty))
            case None                  => None
          }

          (il.InteractionFailureStrategy.RetryWithIncrementalBackoff(initialTimeout, backoffFactor, maximumRetries, maxTimeBetweenRetries, exhaustedRetryEvent), exhaustedRetryEvent)
        case dsl.InteractionFailureStrategy.BlockInteraction => (

          il.InteractionFailureStrategy.BlockInteraction, None)
        case dsl.InteractionFailureStrategy.FireEventAfterFailure(eventNameOption) =>
          val eventName = eventNameOption.getOrElse(interactionDescriptor.name + exhaustedEventAppend)
          val exhaustedRetryEvent: EventDescriptor = EventDescriptor(eventName, Seq.empty)

          (il.InteractionFailureStrategy.FireEventAfterFailure(exhaustedRetryEvent), Some(exhaustedRetryEvent))
      }
    }

    InteractionTransition(
      originalEvents = originalEvents ++ exhaustedRetryEvent,
      requiredIngredients = inputFields.map(IngredientDescriptor(_)),
      name = interactionDescriptor.name,
      originalName = interactionDescriptor.originalName.getOrElse(interactionDescriptor.name),
      predefinedIngredients = predefinedIngredients,
      maximumExecutionCount = interactionDescriptor.maximumExecutionCount,
      failureStrategy = failureStrategy,
      eventOutputTransformers = eventOutputTransformers )
  }
}
