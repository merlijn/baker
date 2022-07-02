package com.ing.baker.il.recipe.petrinet

case class Edge(allowedEventName: Option[String]) {

  def filterToken(e: Any): Boolean = allowedEventName match {
    case None        => true
    case Some(event) => event.equals(e)
  }
}
