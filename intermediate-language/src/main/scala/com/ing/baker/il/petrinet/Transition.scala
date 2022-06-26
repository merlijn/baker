package com.ing.baker.il.petrinet

import com.ing.baker.petrinet.api.Identifiable

object Transition {

  implicit val identifiable: Identifiable[Transition] = p => p.id
}

trait Transition {

  def id: String
  def label: String
}

case class MissingEventTransition(override val label: String) extends Transition {
  override def id: String = s"MissingEventTransition:$label"
}

case class IntermediateTransition(override val label: String) extends Transition {
  override val id: String = s"IntermediateTransition:$label"
}

case class SplitTransition(override val label: String) extends Transition {
  override def id: String = s"MultiFacilitatorTransition:$label"
}
