package com.ing.baker.graph.api

trait Graph[N, E] {
  
  def nodes: Iterable[N]
  
  def edges: Iterable[E]
  
  def adjacency: Map[E, (N, N)]
}
