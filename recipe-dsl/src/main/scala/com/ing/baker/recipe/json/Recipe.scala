package com.ing.baker.recipe.json

import com.ing.baker.recipe.common
import com.ing.baker.recipe.common.InteractionFailureStrategy

import scala.concurrent.duration.FiniteDuration

case class Recipe(name: String, sensoryEvents: Seq[Event], override val interactions: Seq[Interaction]) extends common.Recipe {

  override val defaultFailureStrategy: InteractionFailureStrategy = InteractionFailureStrategy.BlockInteraction()

  override val eventReceivePeriod: Option[FiniteDuration] = None

  override val retentionPeriod: Option[FiniteDuration] = None
}
