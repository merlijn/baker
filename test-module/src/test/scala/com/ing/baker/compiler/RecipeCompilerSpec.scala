package com.ing.baker.compiler

import com.ing.baker.TestRecipeHelper._
import com.ing.baker._
import com.ing.baker.il.{CompiledRecipe, ValidationSettings}
import com.ing.baker.recipe.common.ProvidesNothing
import com.ing.baker.recipe.scaladsl.{Event, Ingredient, Interaction, Recipe}

import scala.language.postfixOps

class RecipeCompilerSpec extends TestRecipeHelper {

  before {
    resetMocks
  }

  "The RecipeCompiler should" should {

    "not have validation errors for a valid recipe" in {
      val recipe: Recipe = getComplexRecipe("ValidRecipe")
      val compiledRecipe: CompiledRecipe = RecipeCompiler.compileRecipe(recipe)
      compiledRecipe.validationErrors shouldBe List.empty
    }

    "give a List of missing ingredients if an interaction has an ingredient that is not provided by any other event or interaction" in {
      val recipe = Recipe("NonProvidedIngredient")
          .withSensoryEvent(secondEvent)
        .withInteractions(interactionOne)

      val compiledRecipe: CompiledRecipe = RecipeCompiler.compileRecipe(recipe)
      compiledRecipe.validationErrors should contain("Ingredient 'initialIngredient' for interaction 'InteractionOne' is not provided by any event or interaction")
    }

    "give a list of wrong ingredients if an predefined ingredient is of the wrong type" in {
      val recipe = Recipe("WrongGivenIngredient")
        .withInteractions(
          interactionOne
            .withRequiredEvent(initialEvent)
            .withPredefinedIngredients(("initialIngredient", Integer.valueOf(12))))
        .withSensoryEvent(initialEvent)

      val compiledRecipe: CompiledRecipe = RecipeCompiler.compileRecipe(recipe)
      compiledRecipe.validationErrors should contain("Predefined argument 'initialIngredient' is not of type: class java.lang.String on interaction: 'InteractionOne'")
    }

    "give a list of wrong ingredients if an predefined ingredient is not needed by the interaction" in {
      val recipe = Recipe("WrongGivenIngredient")
        .withInteractions(
          interactionOne
            .withPredefinedIngredients(("WrongIngredient", null)))
        .withSensoryEvent(initialEvent)

      val compiledRecipe: CompiledRecipe = RecipeCompiler.compileRecipe(recipe)
      compiledRecipe.validationErrors should contain("Predefined argument 'WrongIngredient' is not defined on interaction: 'InteractionOne'")
    }

    "validate if there are unreachable interactions exist or not" ignore {
      val recipe = Recipe("RecipeWithUnreachableInteraction")
        .withInteractions(interactionSeven, interactionEight)
        .withSensoryEvent(initialEvent)

      val compiledRecipe: CompiledRecipe = RecipeCompiler.compileRecipe(recipe,
        ValidationSettings(allowNonexecutableInteractions = false))

      compiledRecipe.validationErrors should contain("InteractionEight is not executable")
    }

    "fail compilation for an empty or null named interaction" in {
      List("", null) foreach { name =>
        val invalidInteraction = Interaction(name, Seq.empty, ProvidesNothing)
        val recipe = Recipe("InteractionNameTest").withInteractions(invalidInteraction).withSensoryEvent(initialEvent)

        intercept[IllegalArgumentException](RecipeCompiler.compileRecipe(recipe)) getMessage() shouldBe "Interaction with a null or empty name found"
      }
    }

    "fail compilation for an empty or null named sieve interaction" in {
      List("", null) foreach { name =>
        val invalidSieveInteraction = Interaction(name, Seq.empty, ProvidesNothing)
        val recipe = Recipe("SieveNameTest").withSieve(invalidSieveInteraction).withSensoryEvent(initialEvent)

        intercept[IllegalArgumentException](RecipeCompiler.compileRecipe(recipe)) getMessage() shouldBe "Sieve Interaction with a null or empty name found"
      }
    }

    "fail compilation for an empty or null named event" in {
      List("", null) foreach { name =>
        val invalidEvent = Event(name)
        val recipe = Recipe("EventNameTest").withSensoryEvent(invalidEvent).withInteraction(interactionOne)

        intercept[IllegalArgumentException](RecipeCompiler.compileRecipe(recipe)) getMessage() shouldBe "Event with a null or empty name found"
      }
    }

    "fail compilation for an empty or null named ingredient" in {
      List("", null) foreach { name =>
        val invalidIngredient = Ingredient(name)
        val recipe = Recipe("IngredientNameTest").withSensoryEvent(Event("someEvent", invalidIngredient)).withInteraction(interactionOne)

        intercept[IllegalArgumentException](RecipeCompiler.compileRecipe(recipe)) getMessage() shouldBe "Ingredient with a null or empty name found"
      }
    }

    "fail compilation for an empty or null named recipe" in {
      List("", null) foreach { name =>
        val recipe = Recipe(name)

        intercept[IllegalArgumentException](RecipeCompiler.compileRecipe(recipe)) getMessage() shouldBe "Recipe with a null or empty name found"
      }
    }

    "fail compilation for an empty/non-logical recipe" in {
      intercept[IllegalArgumentException](RecipeCompiler.compileRecipe(Recipe("someName"))) getMessage() shouldBe "Not a valid recipe: No sensory events found."
      intercept[IllegalArgumentException](RecipeCompiler.compileRecipe(Recipe("someName").withSensoryEvent(initialEvent))) getMessage() shouldBe "Not a valid recipe: No interactions or sieves found."
    }

  }
}
