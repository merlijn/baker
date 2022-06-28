package com.ing.baker.runtime.core

import com.ing.baker.il.recipe.CompiledRecipe
import scala.collection.JavaConverters.*

case class RecipeInformation(recipe: CompiledRecipe,
                             recipeCreatedTime: Long,
                             errors: Set[String])