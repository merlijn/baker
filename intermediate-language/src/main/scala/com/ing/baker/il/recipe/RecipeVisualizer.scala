//package com.ing.baker.il.recipe
//
//import com.ing.baker.il.recipe.RecipeVisualStyle
//import com.ing.baker.il.recipe.petrinet.Place.*
//import com.ing.baker.il.recipe.petrinet.*
//import com.ing.baker.petrinet.api.{DiGraph, PetriNet}
//import com.typesafe.config.{Config, ConfigFactory}
//import org.slf4j.LoggerFactory
//import RecipeVisualizer.GraphViz.*
//
//object RecipeVisualizer {
//
//  object GraphViz {
//
//    object DotAttr {
//      def apply(key: String, value: Any): DotAttr = DotAttr(key, value.toString)
//    }
//
//    case class DotAttr(key: String, value: String)
//    case class DotRoot(directed: Boolean, nodeAttributes: Seq[DotAttr], rootAttributes: Seq[DotAttr])
//
//    case class DotNode(id: String, attributes: Seq[DotAttr])
//
//    case class DotEdge(source: String, target: String, attributes: Seq[DotAttr])
//
//    def createGraphVizRepr[N, E](graph: DiGraph[N, E], root: DotRoot, nodeFn: N => DotNode, edgeFn: E => DotEdge): String = {
//      "TODO"
//    }
//  }
//
//  val log = LoggerFactory.getLogger("com.ing.baker.il.RecipeVisualizer")
//
//  type RecipeEdge = PetriNet.Edge[Place, Transition]
//
//  type RecipeGraph = DiGraph[Place | Transition, RecipeEdge]
//
//  type RecipeNode = Place | Transition
//
//  implicit class RecipePetriNetGraphFns(graph: RecipeGraph) {
//
//    def compactNode(node: RecipeNode): RecipeGraph = {
//
////      val incoming = graph.incomingNodes(node)
////      val outgoing = graph.outgoingNodes(node)
////
////      // create direct edges from all incoming to outgoing nodes
////      val newEdges = incoming.flatMap { in =>
////        outgoing.map(out => PetriNet.Edge[Place, Transition](in, out, 0, None))
////      }
////
////      val removed = graph.removeNodes(graph.incomingNodes(node) ++ graph.outgoingNodes(node) + node)
////
////      // remove the node, removes all it's incoming and outgoing edges and add the new direct edges
////      newEdges.foldLeft(removed) {
////        case (acc, e) => acc.add(e.source, e.target, e)
////      }
//
//      graph
//    }
//
//    def compactAllNodes(predicate: RecipeNode => Boolean): RecipeGraph =
//      graph.nodes.foldLeft(graph) {
//        case (acc, node) if predicate(node) => acc.compactNode(node)
//        case (acc, _)                       => acc
//      }
//  }
//
//  /**
//    * Returns the label for a node.
//    */
//  private def nodeLabelFn: RecipeNode => String = {
//    case p: Place      => p.label.split('.').last
//    case t: Transition => t.label.split('.').last
//  }
//
//  private def nodeIdFn: RecipeNode => String = {
//    case p: Place      => p.label
//    case t: Transition => t.label
//  }
//
//  /**
//    * Returns the style attributes for a node.
//    */
//  private def nodeDotAttrFn(graph: RecipeGraph, style: RecipeVisualStyle): (RecipeNode, Set[String], Set[String]) => List[DotAttr] =
//    (node: RecipeNode, eventNames: Set[String], ingredientNames: Set[String]) => {
//
//      val labelAttr = DotAttr("label", nodeLabelFn(node))
//
//      val styleAttrs = node match {
//        case Place(_, InteractionEventOutputPlace) => style.choiceAttributes
//        case Place(_, EventOrPreconditionPlace) => style.preconditionORAttributes
//        case Place(_, EmptyEventIngredientPlace) => style.emptyEventAttributes
//        case p: Place if graph.incomingNodes(p).isEmpty => style.missingIngredientAttributes
//        case Place(label, _) if ingredientNames contains label => style.providedIngredientAttributes
//        case _: Place => style.ingredientAttributes
//        case t: InteractionTransition if eventNames.intersect(t.events.map(_.name).toSet).nonEmpty => style.firedInteractionAttributes
//        case _: InteractionTransition => style.interactionAttributes
//        case transition: Transition if eventNames.contains(transition.label) => style.eventFiredAttributes
//        case _: SplitTransition => style.choiceAttributes
//        case _: MissingEventTransition => style.eventMissingAttributes
//        case EventTransition(_, true, _) => style.sensoryEventAttributes
//        case _: Transition => style.eventAttributes
//      }
//
//      styleAttrs :+ labelAttr
//    }
//
//  private def recipeDot(graph: RecipeGraph, style: RecipeVisualStyle, filter: String => Boolean, eventNames: Set[String], ingredientNames: Set[String]): String = {
//
//    val myRoot = DotRoot(directed = true,
//      nodeAttributes = style.commonNodeAttributes,
//      rootAttributes = style.rootAttributes)
//
//    def nodeStyleFn(node: RecipeNode): DotNode =
//      DotNode(nodeIdFn(node), nodeDotAttrFn(graph, style)(node, eventNames, ingredientNames))
//
//    def edgeStyleFn(edge: RecipeEdge): DotEdge =
//      DotEdge(nodeIdFn(edge.source), nodeIdFn(edge.target), List.empty)
//
//
//    // specifies which places to compact (remove)
//    val placesToCompact = (node: RecipeNode) => node match {
//      case Place(_, IngredientPlace)           => false
//      case Place(_, EmptyEventIngredientPlace) => false
//      case Place(_, EventOrPreconditionPlace)  => false
//      case Place(_, _)  => true
//      case _ => false
//    }
//
//    // specifies which transitions to compact (remove)
//    val transitionsToCompact = (node: RecipeNode) => node match {
//      case _: IntermediateTransition => true
//      case _: SplitTransition => true
//      case _ => false
//    }
//
//    // compacts all nodes that are not of interest to the recipe
//    val compactedGraph = graph
//      .compactAllNodes(placesToCompact)
//      .compactAllNodes(transitionsToCompact)
//
//    // filters out all the nodes that match the predicate function
//    val filteredGraph = compactedGraph.removeNodes(compactedGraph.nodes.filter(n => !filter(n.toString)))
//
//    // creates the .dot representation
//    createGraphVizRepr(
//      graph = graph,
//      root = myRoot,
//      edgeFn = edgeStyleFn,
//      nodeFn = nodeStyleFn)
//  }
//
//
//  def visualizeRecipe(recipe: CompiledRecipe,
//                      config: Config = ConfigFactory.load(),
//                      filter: String => Boolean = _ => true,
//                      eventNames: Set[String] = Set.empty,
//                      ingredientNames: Set[String] = Set.empty): String =
//
//    recipeDot(recipe.petriNet, new RecipeVisualStyle(config), filter, eventNames, ingredientNames)
//
//
//  def visualizePetriNet[P, T, E](graph: PetriNet[P, T, E]): String = {
//
//    val nodeLabelFn: P | T => String = node => node.toString
//
//    val nodeDotAttrFn: P | T => List[DotAttr] = node => List.empty
////    match {
////      case p: Place      => List(DotAttr("shape", "circle"))
////      case t: Transition => List(DotAttr("shape", "square"))
////    }
//
//    val myRoot = DotRoot(
//      directed = true,
//      nodeAttributes = List.empty,
//      rootAttributes = List.empty)
//
//    def nodeStyleFn(node: P | T): DotNode =
//      DotNode(nodeLabelFn(node), nodeDotAttrFn(node))
//
//    def edgeStyleFn(edge: PetriNet.Edge[P, T]): DotEdge =
//      DotEdge(nodeLabelFn(edge.source), nodeLabelFn(edge.target), List.empty)
//
//    // creates the .dot representation
//    createGraphVizRepr(
//      graph = graph,
//      root = myRoot,
//      edgeFn = edgeStyleFn,
//      nodeFn = nodeStyleFn)
//  }
//}
