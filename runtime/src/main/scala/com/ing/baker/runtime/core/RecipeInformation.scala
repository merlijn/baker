package com.ing.baker.runtime.core

import com.ing.baker.il.CompiledRecipe

import scala.collection.JavaConverters._

case class RecipeInformation(recipe: CompiledRecipe,
                             recipeCreatedTime: Long,
                             errors: Set[String])