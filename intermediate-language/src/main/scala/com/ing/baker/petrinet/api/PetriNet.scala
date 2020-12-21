package com.ing.baker.petrinet.api


case class Edge[P, T](source: Either[P, T], target: Either[P, T], weight: Int, label: Option[Any])

/**
 * Petri net class.
  *
 * Backed by a graph object from scala-graph (https://github.com/scala-graph/scala-graph)
 */
case class PetriNet[P, T](places: Set[P], transitions: Set[T], edges: Set[Edge[P, T]]) {
  
  def removePlace(p: P): PetriNet[P, T] = {
    
    val newEdges = edges.filter {
      case Edge(_, Left(p), _, _) => true
      case Edge(Left(p), _, _, _) => true
      case _ => false
    }

    PetriNet[P, T](places - p, transitions, edges)
  }
  
  def removeTransition(t: T): PetriNet[P, T] = {
    
    val newEdges = edges.filter {
      case Edge(Right(t), _, _, _) => true
      case Edge(_, Right(t), _, _) => true
      case _ => false
    }
    
    PetriNet[P, T](places, transitions - t, edges)
  }
  
  def removeTransitions(transitions: Iterable[T]) = transitions.foldLeft(this) {
    case (pn, t) => pn.removeTransition(t)
  }

  def removePlaces(places: Iterable[P]) = places.foldLeft(this) {
    case (pn, p) => pn.removePlace(p)
  }
  
  def isConnected():Boolean = ???
  
  def findCycles(): Seq[Either[P, T]] = ???

  /**
    * The out-adjecent places of a transition.
    *
    * @param t transition
    * @return
    */
  def outgoingPlaces(t: T): Set[P] = outMarking(t).keySet

  /**
    * The out-adjacent transitions of a place.
    *
    * @param p place
    * @return
    */
  def outgoingTransitions(p: P): Set[T] = edges.collect {
    case Edge(Left(`p`), Right(t), _, _) => t 
  }.toSet

  /**
    * The in-adjacent places of a transition.
    *
    * @param t transition
    * @return
    */
  def incomingPlaces(t: T): Set[P] = inMarking(t).keySet

  /**
    * The in-adjacent transitions of a place.
    *
    * @param p place
    * @return
    */
  def incomingTransitions(p: P): Set[T] = edges.collect {
    case Edge(Right(t), Left(`p`), _, _) => t
  }.toSet

  /**
    * The set of nodes (places + transitions) in the petri net.
    *
    * @return The set of nodes.
    */
  def nodes: scala.collection.Set[Either[P, T]] = 
    transitions.map[Either[P, T]](Right(_)) ++ places.map[Either[P, T]](Left(_))

  /**
    * Returns the in-marking of a transition. That is; a map of place -> arc weight
    *
    * @param t transition
    * @return
    */
  def inMarking(t: T): MultiSet[P] = edges.collect {
    case Edge(Left(p), Right(`t`), weight, _) => p -> weight
  }.toMap

  /**
    * The out marking of a transition. That is; a map of place -> arc weight
    *
    * @param t transition
    * @return
    */
  def outMarking(t: T): MultiSet[P] = edges.collect {
    case Edge(Right(`t`), Left(p), weight, _) => p -> weight
  }.toMap

  /**
    * Returns the (optional) edge for a given place -> transition pair.
    *
    * @param from The source place.
    * @param to The target transition.
    * @return
    */
  def findPTEdge(from: P, to: T): Option[Any] = edges.collectFirst {
    case Edge(Left(`from`), Right(`to`), _, label) => label
  }

  /**
    * Returns the (optional) edge for a given transition -> place pair.
    *
    * @param from The source transition.
    * @param to The target place.
    * @return
    */
  def findTPEdge(from: T, to: P): Option[Any] = edges.collectFirst {
    case Edge(Left(`from`), Right(`to`), _, label) => label
  }
}