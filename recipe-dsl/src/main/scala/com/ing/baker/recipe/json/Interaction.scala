package com.ing.baker.recipe.json

import com.ing.baker.recipe.common
import com.ing.baker.recipe.common._
import com.ing.baker.types.Converters

case class Interaction private(override val name: String,
                               override val originalName: Option[String],
                               override val input: Seq[Ingredient],
                               override val output: Seq[Event],
                               override val maximumExecutionCount: Option[Int] = None)

  extends common.InteractionDescriptor {

  override val predefinedIngredients: Map[String, com.ing.baker.types.Value] = Map.empty

  override val eventOutputTransformers: Map[common.Event, common.EventOutputTransformer] = Map.empty

  override val renamedInputIngredients: Map[String, String] = Map.empty

  override val failureStrategy: Option[InteractionFailureStrategy] = None

  override val requiredEvents: Set[String] = Set.empty

  override val requiredOneOfEvents: Set[Set[String]] = Set.empty
}
