package com.ing.baker.runtime.core

import akka.actor.ActorSystem
import com.ing.baker.*
import com.ing.baker.compiler.RecipeCompiler
import com.ing.baker.recipe.dsl.examples.TestRecipe.*
import com.ing.baker.recipe.dsl.Recipe
import com.ing.baker.runtime.core.implementations.{InteractionOneFieldName, InteractionOneInterfaceImplementation, InteractionOneWrongApply}

import scala.language.postfixOps

class BakerSetupSpec extends BakerRuntimeTestBase {

  override def actorSystemName = "BakerSetupSpec"

  before {
    resetMocks
  }

  "The Baker execution engine during setup" should {

    "bootstrap correctly without throwing an error if provided a correct recipe and correct implementations" when {

      "providing implementations in a sequence" in {

        val baker = new Baker()

        baker.addImplementationMethods(mockImplementations)
      }

      "providing an implementation with the class simplename same as the interaction" in {

        val baker = new Baker()

        baker.addImplementationMethod(new implementations.InteractionOne())
      }

      "providing an implementation for a renamed interaction" in {

        val recipe = Recipe("simpleNameImplementationWithRename")
          .withInteractions((interactionOne.withName("interactionOneRenamed")))
          .withSensoryEvent(initialEvent)

        val baker = new Baker()

        baker.addImplementationMethod(new implementations.InteractionOne())

        baker.addRecipe(RecipeCompiler.compileRecipe(recipe))
      }

      "providing an implementation with a name field" in {

        val recipe = Recipe("fieldNameImplementation")
          .withInteractions(interactionOne)
          .withSensoryEvent(initialEvent)

        val baker = new Baker()

        baker.addImplementationMethod(new InteractionOneFieldName())

        baker.addRecipe(RecipeCompiler.compileRecipe(recipe))
      }

      "providing the implementation in a sequence with the interface its implementing with the correct name" in {

        val recipe = Recipe("interfaceImplementation")
          .withInteractions(interactionOne)
          .withSensoryEvent(initialEvent)

        val baker = new Baker()

        baker.addImplementationMethod(new InteractionOneInterfaceImplementation())

        baker.addRecipe(RecipeCompiler.compileRecipe(recipe))
      }

      "the recipe contains complex ingredients that are serializable" in {
        val recipe = Recipe("complexIngredientInteractionRecipe")
          .withInteractions(complexIngredientInteraction)
          .withSensoryEvent(initialEvent)

        val baker = new Baker()

        baker.addImplementationMethod(mock[ComplexIngredientInteraction])

        baker.addRecipe(RecipeCompiler.compileRecipe(recipe))
      }
    }

    "throw a exception" when {
      "an invalid recipe is given" in {

        val recipe = Recipe("NonProvidedIngredient")
          .withInteractions(interactionOne)
          .withSensoryEvent(secondEvent)

        val baker = new Baker()

        baker.addImplementationMethods(mockImplementations)

        intercept[IllegalArgumentException] {
          baker.addRecipe(RecipeCompiler.compileRecipe(recipe))
        } should have('message ("Ingredient 'initialIngredient' for interaction 'InteractionOne' is not provided by any event or interaction"))
      }

      "a recipe does not provide an implementation for an interaction" in withActorSystem(ActorSystem("MissingImplementation")) { actorSystem =>

        val baker = new Baker()(actorSystem)

        val recipe = Recipe("MissingImplementation")
          .withInteractions(interactionOne)
          .withSensoryEvent(initialEvent)

        intercept[IllegalStateException] {
          baker.addRecipe(RecipeCompiler.compileRecipe(recipe))
        } should have('message ("Missing interaction implementations: InteractionOne"))
      }

      // TODO uncheck ignore when fixed
      "a recipe provides an implementation for an interaction and does not comply to the Interaction" ignore {

        val recipe = Recipe("WrongImplementation")
          .withInteractions(interactionOne)
          .withSensoryEvent(initialEvent)

        val baker = new Baker()

        baker.addImplementationMethod(new InteractionOneWrongApply())

        intercept[IllegalStateException] {
          baker.addRecipe(RecipeCompiler.compileRecipe(recipe))
        } should have('message ("Missing interaction implementations: InteractionOne"))
      }
    }
  }
}
