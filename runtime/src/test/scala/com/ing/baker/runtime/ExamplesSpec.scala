package com.ing.baker.runtime

import com.ing.baker.BakerRuntimeTestBase
import com.ing.baker.compiler.RecipeCompiler

class ExamplesSpec extends BakerRuntimeTestBase  {
  override def actorSystemName = "ExamplesSpec"

  "The WebShop recipe" should {

    import com.ing.baker.recipe.dsl.examples.Webshop._

    "compile without validation errors" in {

      // compiles the recipe
      val compiledRecipe = RecipeCompiler.compileRecipe(webShopRecipe)

//      println(s"Visual recipe: ${compiledRecipe.getRecipeVisualization}")

      // prints any validation errors the compiler found
      compiledRecipe.validationErrors shouldBe empty
    }
  }

  "The open account recipe" should {

    import com.ing.baker.recipe.dsl.examples.OpenAccount._

    "compile without validation errors" in {

      // compiles the recipe
      val compiledRecipe = RecipeCompiler.compileRecipe(openAccountRecipe)

      // prints any validation errors the compiler found
      compiledRecipe.validationErrors shouldBe empty
    }
  }
}
