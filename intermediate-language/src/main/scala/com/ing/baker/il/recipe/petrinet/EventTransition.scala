package com.ing.baker.il.recipe.petrinet

import com.ing.baker.il.recipe.petrinet.Transition
import com.ing.baker.il.recipe.EventDescriptor

/**
  * Transition providing data from an event.
  */
case class EventTransition(event: EventDescriptor,
                           isSensoryEvent: Boolean = true,
                           maxFiringLimit: Option[Int] = None) extends Transition {

  override val label: String = event.name
  override val id: String = s"Event:$label"
  override val toString: String = label
}
