package com.ing.baker.runtime

import com.ing.baker.compiler.RecipeCompiler
import org.scalatest.{Matchers, WordSpecLike}

class ExamplesSpec extends  WordSpecLike with Matchers  {

  "The WebShop recipe" should {

    import com.ing.baker.recipe.dsl.examples.Webshop.*

    "compile without validation errors" in {

      // compiles the recipe
      val compiledRecipe = RecipeCompiler.compileRecipe(webShopRecipe)

//      println(s"Visual recipe: ${compiledRecipe.getRecipeVisualization}")

      // prints any validation errors the compiler found
      compiledRecipe.validationErrors shouldBe empty
    }
  }

  "The open account recipe" should {

    import com.ing.baker.recipe.dsl.examples.OpenAccount.*

    "compile without validation errors" in {

      // compiles the recipe
      val compiledRecipe = RecipeCompiler.compileRecipe(openAccountRecipe)

      // prints any validation errors the compiler found
      compiledRecipe.validationErrors shouldBe empty
    }
  }
}
