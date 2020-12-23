package com.ing.baker.il

import com.ing.baker.il.RecipeVisualizer.Dot._
import com.ing.baker.il.petrinet.Place._
import com.ing.baker.il.petrinet._
import com.ing.baker.petrinet.api._
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory

import scala.language.higherKinds

object RecipeVisualizer {
  
  object Dot {
    
    object DotAttr {
      def apply(key: String, value: Double): DotAttr = DotAttr(key, value.toString)
    }
    
    case class DotAttr(key: String, value: String)
    case class DotRoot(directed: Boolean, nodeAttributes: Seq[DotAttr], rootAttributes: Seq[DotAttr])

    case class DotNode(id: String, attributes: Seq[DotAttr])
    
    case class DotEdge(source: String, target: String, attributes: Seq[DotAttr])

    def createDotRepr[N, E](graph: DiGraph[N, E], root: DotRoot, nodeFn: N => DotNode, edgeFn: E => DotEdge): String = {
      "TODO"
    }
  }

  val log = LoggerFactory.getLogger("com.ing.baker.il.RecipeVisualizer")

  type RecipeEdge = PetriNet.Edge[Place, Transition]

  type RecipePetriNet = DiGraph[Either[Place, Transition], RecipeEdge]
  
  type RecipeNode = Either[Place, Transition]
  
  implicit class RecipePetriNetGraphFns(graph: RecipePetriNet) {

    def compactNode(node: Either[Place, Transition]): RecipePetriNet = {

      val incoming = graph.incomingNodes(node)
      val outgoing = graph.outgoingNodes(node)
      
      // create direct edges from all incoming to outgoing nodes
      val newEdges = incoming.flatMap { in =>
        outgoing.map(out => PetriNet.Edge[Place, Transition](in, out, 0, None))
      }
      
      val removed = graph.removeNodes(graph.incomingNodes(node) ++ graph.outgoingNodes(node) + node)

      // remove the node, removes all it's incoming and outgoing edges and add the new direct edges
      newEdges.foldLeft(removed) {
        case (acc, e) => acc.add(e.source, e.target, e) 
      }
    }

    def compactAllNodes(predicate: Either[Place, Transition] => Boolean): RecipePetriNet =
      graph.nodes.foldLeft(graph) {
        case (acc, node) if predicate(node) => acc.compactNode(node)
        case (acc, _)                       => acc
      }
  }

  /**
    * Returns the label for a node.
    */
  private def nodeLabelFn: Either[Place, Transition] => String = {
    case Left(place)       => place.label.split('.').last
    case Right(transition) => transition.label.split('.').last
  }

  private def nodeIdFn: Either[Place, Transition] => String = {
    case Left(place)       => place.label
    case Right(transition) => transition.label
  }

  /**
    * Returns the style attributes for a node.
    */
  private def nodeDotAttrFn(graph: RecipePetriNet, style: RecipeVisualStyle): (RecipeNode, Set[String], Set[String]) => List[DotAttr] =
    (node: RecipeNode, eventNames: Set[String], ingredientNames: Set[String]) => {

      val labelAttr = DotAttr("label", nodeLabelFn(node))

      val styleAttrs = node match {
        case Left(Place(_, InteractionEventOutputPlace)) => style.choiceAttributes
        case Left(Place(_, EventOrPreconditionPlace)) => style.preconditionORAttributes
        case Left(Place(_, EmptyEventIngredientPlace)) => style.emptyEventAttributes
        case p @ Left(_) if graph.incomingNodes(p).isEmpty => style.missingIngredientAttributes
        case Left(Place(label, _)) if ingredientNames contains label => style.providedIngredientAttributes
        case Left(_) => style.ingredientAttributes
        case Right(t: InteractionTransition) if eventNames.intersect(t.events.map(_.name).toSet).nonEmpty => style.firedInteractionAttributes
        case Right(_: InteractionTransition) => style.interactionAttributes
        case Right(transition: Transition) if eventNames.contains(transition.label) => style.eventFiredAttributes
        case Right(_: MultiFacilitatorTransition) => style.choiceAttributes
        case Right(_: MissingEventTransition) => style.eventMissingAttributes
        case Right(EventTransition(_, true, _)) => style.sensoryEventAttributes
        case Right(_) => style.eventAttributes
      }

      styleAttrs :+ labelAttr
    }

  private def recipeDot(graph: RecipePetriNet, style: RecipeVisualStyle, filter: String => Boolean, eventNames: Set[String], ingredientNames: Set[String]): String = {

    val myRoot = DotRoot(directed = true,
      nodeAttributes = style.commonNodeAttributes,
      rootAttributes = style.rootAttributes)

    def nodeStyleFn(node: RecipeNode): DotNode = 
      DotNode(nodeIdFn(node), nodeDotAttrFn(graph, style)(node, eventNames, ingredientNames))

    def edgeStyleFn(edge: RecipeEdge): DotEdge =
      DotEdge(nodeIdFn(edge.source), nodeIdFn(edge.target), List.empty)
    

    // specifies which places to compact (remove)
    val placesToCompact = (node: RecipeNode) => node match {
      case Left(Place(_, IngredientPlace))           => false
      case Left(Place(_, EmptyEventIngredientPlace)) => false
      case Left(Place(_, EventOrPreconditionPlace))  => false
      case Left(Place(_, _))  => true
      case _ => false
    }

    // specifies which transitions to compact (remove)
    val transitionsToCompact = (node: RecipeNode) => node match {
      case Right(transition: Transition) => transition.isInstanceOf[IntermediateTransition] || transition.isInstanceOf[MultiFacilitatorTransition]
      case _ => false
    }

    // compacts all nodes that are not of interest to the recipe
    val compactedGraph = graph
      .compactAllNodes(placesToCompact)
      .compactAllNodes(transitionsToCompact)

    // filters out all the nodes that match the predicate function
    val filteredGraph = compactedGraph.removeNodes(compactedGraph.nodes.filter(n => !filter(n.toString)))

    // creates the .dot representation
    createDotRepr(
      graph = graph,
      root = myRoot,
      edgeFn = edgeStyleFn,
      nodeFn = nodeStyleFn)
  }


  def visualizeRecipe(recipe: CompiledRecipe,
                      config: Config = ConfigFactory.load(),
                      filter: String => Boolean = _ => true,
                      eventNames: Set[String] = Set.empty,
                      ingredientNames: Set[String] = Set.empty): String =

    recipeDot(recipe.petriNet, new RecipeVisualStyle(config), filter, eventNames, ingredientNames)


  def visualizePetriNet[P, T](graph: PetriNet[P, T]): String = {

    val nodeLabelFn: Either[P, T] => String = node => node match {
      case Left(p)  => p.toString
      case Right(t) => t.toString
    }

    val nodeDotAttrFn: Either[P, T] => List[DotAttr] = node => node match {
      case Left(_)  => List(DotAttr("shape", "circle"))
      case Right(_) => List(DotAttr("shape", "square"))
    }

    val myRoot = DotRoot(
      directed = true,
      nodeAttributes = List.empty,
      rootAttributes = List.empty)

    def nodeStyleFn(node: Either[P, T]): DotNode = DotNode(nodeLabelFn(node), nodeDotAttrFn(node))

    def edgeStyleFn(edge: PetriNet.Edge[P, T]): DotEdge = DotEdge(nodeLabelFn(edge.source), nodeLabelFn(edge.target), List.empty)
    
    // creates the .dot representation
    createDotRepr(
      graph = graph,
      root = myRoot,
      edgeFn = edgeStyleFn,
      nodeFn = nodeStyleFn)
  }
}
