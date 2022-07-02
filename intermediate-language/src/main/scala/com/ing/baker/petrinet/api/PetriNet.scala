package com.ing.baker.petrinet.api

import PetriNet.*
import scala.reflect.Typeable

object PetriNet {

  sealed trait Edge[P, T] {
    val source: P | T
    val target: P | T
    val weight: Int
  }

  case class PTEdge[P, T](override val source: P, override val target: T, override val weight: Int, label: Option[Any]) extends Edge[P, T]
  case class TPEdge[P, T](override val source: T, override val target: P, override val weight: Int, label: Option[Any]) extends Edge[P, T]

  def apply[P : Typeable, T : Typeable](edges: Set[Edge[P, T]]): PetriNet[P, T] = {
    
    val places = edges.collect {
      case TPEdge(_, p, _, _) => p
      case PTEdge(p, _, _, _) => p
    }
    
    val transitions = edges.collect {
      case TPEdge(t, _, _, _) => t
      case PTEdge(_, t, _, _) => t
    }

    val innerNodes: Set[Either[P, T]] = edges.flatMap {
      case TPEdge(t, p, _, _) => Set(Left(p), Right(t))
      case PTEdge(p, t, _, _) => Set(Left(p), Right(t))
    }
    
    PetriNet[P, T](innerNodes, edges)
  }
}

/**
 * Petri net class.
 */
case class PetriNet[P : Typeable : Not[T], T : Typeable : Not[P]]
  (private val innerNodes: Set[Either[P,T]], innerEdges: Set[Edge[P, T]]) extends DiGraph[P | T, Edge[P, T]] {
  
  type InnerEdge = Edge[P, T]

  def places: Iterable[P] = innerNodes.view.collect { case Left(p) => p }

  def transitions: Iterable[T] = innerNodes.view.collect { case Right(t) => t }

  /**
    * The set of nodes (places + transitions) in the petri net.
    *
    * @return The set of nodes.
    */
  override def nodes: Iterable[P | T] = innerNodes.map {
    case Left(p) => p
    case Right(t) => t
  }

  override def edges: Iterable[Edge[P, T]] = innerEdges

  override def add(a: P | T, b: P | T, e: PetriNet.Edge[P, T]): DiGraph[P | T, Edge[P, T]] = ???

  def removePlace(p: P): PetriNet[P, T] = {

    val newEdges = innerEdges.filter(e => e.source == p || e.target == p)

    PetriNet[P, T](innerNodes - Left(p), newEdges)
  }
  
  def removeTransition(t: T): PetriNet[P, T] = {
    
    val newEdges = innerEdges.filter(e => e.source == t || e.target == t)
    
    PetriNet[P, T](innerNodes - Right(t), newEdges)
  }
  
  def removeTransitions(transitions: Iterable[T]) = transitions.foldLeft(this) {
    case (pn, t) => pn.removeTransition(t)
  }

  def removePlaces(places: Iterable[P]) = places.foldLeft(this) {
    case (pn, p) => pn.removePlace(p)
  }

  def findCycles(): Iterable[List[P | T]] = ???

  def isConnected(): Boolean = ???

  override def incomingNodes(n: P | T): Set[P | T] = ???

  override def outgoingNodes(n: P | T): Set[P | T] = ???

  override def removeNode(n: P | T): PetriNet[P, T] = {

    val newEdges = innerEdges.filter(e => e.source == n || e.target == n)

    n match {
      case p: P => PetriNet[P, T](innerNodes - Left(p), newEdges)
      case t: T => PetriNet[P, T](innerNodes - Right(t), newEdges)
    }
  }

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
  def outgoingTransitions(p: P): Set[T] = innerEdges.collect {
    case PTEdge(`p`, t, _, _) => t
  }

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
  def incomingTransitions(p: P): Set[T] = innerEdges.collect {
    case TPEdge(t, `p`, _, _) => t
  }



  /**
    * Returns the in-marking of a transition. That is; a map of place -> arc weight
    *
    * @param t transition
    * @return
    */
  def inMarking(t: T): MultiSet[P] = edges.collect {
    case PTEdge(p, `t`, weight, _) => p -> weight
  }.toMap

  /**
    * The out marking of a transition. That is; a map of place -> arc weight
    *
    * @param t transition
    * @return
    */
  def outMarking(t: T): MultiSet[P] = edges.collect {
    case TPEdge(`t`, p, weight, _) => p -> weight
  }.toMap

  /**
    * Returns the (optional) edge for a given place -> transition pair.
    *
    * @param from The source place.
    * @param to The target transition.
    * @return
    */
  def findPTEdge(from: P, to: T): Option[Any] = edges.collectFirst {
    case PTEdge(`from`, `to`, _, label) => label
  }

  /**
    * Returns the (optional) edge for a given transition -> place pair.
    *
    * @param from The source transition.
    * @param to The target place.
    * @return
    */
  def findTPEdge(from: T, to: P): Option[Any] = edges.collectFirst {
    case TPEdge(`from`, `to`, _, label) => label
  }
}