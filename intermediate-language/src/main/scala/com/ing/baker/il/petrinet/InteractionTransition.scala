package com.ing.baker.il.petrinet

import com.ing.baker.il
import com.ing.baker.il.{EventOutputTransformer, InteractionFailureStrategy, _}
import com.ing.baker.types.Value
import org.slf4j._


/**
  * A transition that represents an Interaction
  */
case class InteractionTransition(originalEvents: Seq[EventDescriptor],
                                 requiredIngredients: Seq[IngredientDescriptor],
                                 name: String,
                                 originalName: String,
                                 predefinedIngredients: Map[String, Value],
                                 maximumExecutionCount: Option[Int],
                                 failureStrategy: InteractionFailureStrategy,
                                 eventOutputTransformers: Map[String, EventOutputTransformer] = Map.empty)

  extends Transition {

  /**
    * The output events for the interaction.
    *
    * They are calculated from the 'original' events.
    *
    * In case an event transformer is found it is applied on the event, otherwise the original is kept.
    */
  val events: Seq[EventDescriptor] = originalEvents.map(e => eventOutputTransformers.get(e.name).map(_.apply(e)).getOrElse(e))

  override val label: String = name

  override val id: Long = il.sha256HashCode(s"InteractionTransition:$label")

  override def toString: String = label

    /**
    * These are the ingredients that are not pre-defined or processId
    */
  val nonProvidedIngredients: Seq[IngredientDescriptor] =
    requiredIngredients.filterNot(i => i.name == processIdName || predefinedIngredients.keySet.contains(i.name))
}
