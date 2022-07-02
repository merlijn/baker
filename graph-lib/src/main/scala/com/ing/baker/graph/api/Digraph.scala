package com.ing.baker.graph.api

import scala.language.implicitConversions
import scala.util.NotGiven

opaque type Weighted[E] = E => Int

object Weighted {
  def apply[E](fn: E => Int): Weighted[E] = fn
}

/**
  * Directed Graph
  * 
  * @tparam N The node/vertice type
  * @tparam E The edge/arc type
  */
trait DiGraph[N, E] {

  def nodes: Iterable[N]

  def edges: Iterable[E]

  def incomingNodes(n: N): Set[N]

  def outgoingNodes(n: N): Set[N]

  def removeNode(n: N): DiGraph[N, E]

  def removeNodes(nodes: Iterable[N]): DiGraph[N, E] =
    nodes.foldLeft(this) { case (acc, n) => acc.removeNode(n) }

  def add(source: N, target: N, e: E): DiGraph[N, E]
}
