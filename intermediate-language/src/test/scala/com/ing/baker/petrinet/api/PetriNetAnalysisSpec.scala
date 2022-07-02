package com.ing.baker.petrinet.api

import org.scalatest.*
import org.scalatest.matchers.*

object DSL {
  /**
    * Type alias for the node type of the scalax.collection.Graph backing the petri net.
    */
  type Node = Either[Place, Transition]

  type Place = Int

  type Transition = Int

  type SimpleMarking = MultiSet[Int]
  
  case class TransitionAdjacency(in: SimpleMarking, out: SimpleMarking)
  
  def marking(n1: Int): SimpleMarking = Map(n1 -> 1) 
  
  def marking(n1: Int, n2: Int): SimpleMarking = Map(n1 -> 1, n2 -> 1)

  def marking(seq: Seq[Int]): SimpleMarking = seq.map(i => i -> 1).toMap
    
  extension(in: SimpleMarking) {
    def ~|~>(out: SimpleMarking): TransitionAdjacency = TransitionAdjacency(in, out)
  }
  
  def seq(n: Int, start: Int = 1): Seq[TransitionAdjacency] = 
    (start to (start + n)).map(i => marking(i) ~|~> marking(i + 1))

  def branch(branchFactor: Int, start: Int = 1): TransitionAdjacency = { 
    val end = ((start + 1) to (start + branchFactor))
    marking(start) ~|~> marking(end)
  }

  def tree(branchFactor: Int, depth: Int, start: Int = 1): Seq[TransitionAdjacency] = {

    if (depth == 0)
      Seq.empty
    else {
      val b = branch(branchFactor, start)
      b.out.keys.foldLeft(Seq(b)) {
        case (accTree, n) =>
          val subTreeRoot = accTree.flatMap(a => a.in.keys ++ a.out.keys).max + 1
          val subTree = tree(branchFactor, depth - 1, subTreeRoot)
          val adjenceny = marking(n) ~|~> marking(subTreeRoot)
          accTree ++ subTree :+ adjenceny
      }
    }
  }

  def createPetriNet(adjacencies: TransitionAdjacency*): PetriNet[Place, Transition] = {
    val edges: Seq[PetriNet.InnerEdge[Place, Transition]] = adjacencies.toSeq.zipWithIndex.flatMap {
      case (a, t) =>
        a.in.map  { case (p, weight) => PetriNet.Edge[Place, Transition](Left(p), Right(t + 1), weight, None) }.toSeq ++
        a.out.map { case (p, weight) => PetriNet.Edge[Place, Transition](Right(t + 1), Left(p), weight, None) }.toSeq
    }

    PetriNet(edges.toSet)
  }
}

class PetriNetAnalysisSpec extends wordspec.AnyWordSpec with should.Matchers {

  import DSL.{given, _}
  
  "The PetriNetAnalysis class" should {

    "correctly asses the reachability of a very simple petri net A" in {

      val boundedNet = createPetriNet(
        marking(1)    ~|~> marking(2, 3),
        marking(2)    ~|~> marking(4),
        marking(3)    ~|~> marking(5),
        marking(4, 5) ~|~> marking(6),
        marking(1)    ~|~> marking(7)
      )

      val initialMarking = Map(1 -> 1)

      val tree = PetriNetAnalysis.calculateCoverabilityTree(boundedNet, initialMarking)

      tree.isCoverable(Map(2 -> 1, 3 -> 1, 4 -> 1)) should be(false)
      tree.isCoverable(Map(4 -> 1)) should be(true)
      tree.isCoverable(Map(5 -> 1)) should be(true)
      tree.isCoverable(Map(6 -> 1)) should be(true)
      tree.isCoverable(Map(6 -> 1, 1 -> 1)) should be(false)
    }

    "be able to create the coverability tree" in {

      val unboundedNet = createPetriNet(
        marking(1) ~|~> marking(1, 2)
      )

      val tree = PetriNetAnalysis.calculateCoverabilityTree(unboundedNet, Map(1 -> 1))

      tree.isCoverable(Map(1 -> 1, 2 -> 10)) should be(true)
    }
  }
}
