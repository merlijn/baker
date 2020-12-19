package com.ing.baker.il.petrinet

case class Edge(allowedEventName: Option[String]) {

  def isTokenAllowed(e: Any): Boolean = allowedEventName match {
    case None        => true
    case Some(event) => event.equals(e)
  }
}
