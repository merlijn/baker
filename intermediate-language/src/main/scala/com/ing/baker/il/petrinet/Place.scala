package com.ing.baker.il.petrinet

import com.ing.baker.il
import com.ing.baker.il.petrinet.Place.PlaceType
import com.ing.baker.petrinet.api.Identifiable

object Place {

  sealed trait PlaceType

  case object IngredientPlace extends PlaceType
  case object InteractionEventOutputPlace extends PlaceType
  case class  FiringLimiterPlace(maxLimit: Int) extends PlaceType
  case object EventPreconditionPlace extends PlaceType
  case object EventOrPreconditionPlace extends PlaceType
  case object IntermediatePlace extends PlaceType
  case object EmptyEventIngredientPlace extends PlaceType
  case object MultiTransitionPlace extends PlaceType

  implicit val identifiable: Identifiable[Place] = p => p.id
}

case class Place(label: String, placeType: PlaceType) {

  val id: Long = il.sha256HashCode(s"$placeType:$label")
}
