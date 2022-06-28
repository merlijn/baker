package com.ing.baker.il.recipe

import com.ing.baker.il.recipe.RecipeValidationSettings
import com.ing.baker.il.recipe.petrinet.InteractionTransition
import com.ing.baker.petrinet.api.PetriNetAnalysis
import com.ing.baker.petrinet.api.multiplicities

import scala.collection.mutable

object RecipeValidations {

  def validateInteraction(compiledRecipe: CompiledRecipe)(interactionTransition: InteractionTransition): Seq[String] = {

    val validationErrors = mutable.ListBuffer.empty[String]

    if (compiledRecipe.petriNet.inMarking(interactionTransition).isEmpty)
      validationErrors += s"Interaction $interactionTransition does not have any requirements (ingredients or preconditions)! This will result in an infinite execution loop."

    // check if the process id argument type is correct

    // TODO move type checking to dsl and scala compiler
//    interactionTransition.requiredIngredients.filter(id => id.name.equals(processIdName)).map {
//      case IngredientDescriptor(_ , types.CharArray)  =>
//      case IngredientDescriptor(_ , incompatibleType) => validationErrors += s"Non supported process id type: ${incompatibleType} on interaction: '$interactionTransition'"
//    }

    // check if the predefined ingredient is of the expected type
    interactionTransition.predefinedIngredients.foreach {
      case (name, value) =>
        interactionTransition.requiredIngredients.find(_.name == name) match {
          case None =>
            validationErrors += s"Predefined argument '$name' is not defined on interaction: '$interactionTransition'"
          // TODO move type checking to dsl and scala compiler
//          case Some(ingredientDescriptor) if !value.isInstanceOf(ingredientDescriptor.`type`) =>
//            validationErrors += s"Predefined argument '$name' is not of type: ${ingredientDescriptor.`type`} on interaction: '$interactionTransition'"
          case _ =>
        }
    }

    validationErrors.toSeq
  }

  def validateInteractions(compiledRecipe: CompiledRecipe): Seq[String] = {
    compiledRecipe.interactionTransitions.toSeq.flatMap(validateInteraction(compiledRecipe))
  }


  /**
    * Validates that all required ingredients for interactions are provided for and of the correct type.
    */
  def validateInteractionIngredients(compiledRecipe: CompiledRecipe): Seq[String] = {
    compiledRecipe.interactionTransitions.toSeq.flatMap { t =>
      t.nonProvidedIngredients.flatMap {
        case (IngredientDescriptor(name)) =>
          compiledRecipe.allIngredients.find(_.name == name) match {
            case None => Some(s"Ingredient '$name' for interaction '${t.name}' is not provided by any event or interaction")
            // TODO move type checking to dsl and scala compiler
//            case Some(IngredientDescriptor(name, ingredientType)) if !expectedType.isAssignableFrom(ingredientType) =>
//              Some(s"Interaction '$t' expects ingredient '$name:$expectedType', however incompatible type: '$ingredientType' was provided")
            case _ => None
          }
      }
    }
  }

  def validateNoCycles(compiledRecipe: CompiledRecipe): Seq[String] = {
    compiledRecipe.petriNet.findCycles().map(c => s"The petrinet topology contains a cycle: $c").toList
  }

  def validateAllInteractionsExecutable(compiledRecipe: CompiledRecipe): Seq[String] = {
    val rootNode = PetriNetAnalysis.calculateCoverabilityTree(compiledRecipe.petriNet, compiledRecipe.initialMarking.multiplicities)

    compiledRecipe.interactionTransitions
      .filterNot(interaction => rootNode.isCoverable(compiledRecipe.petriNet.inMarking(interaction)))
      .map (interaction => s"$interaction is not executable")
      .toSeq
  }

  /**
    * Validates the compiled recipe.
    *
    * The validations are:
    *   1. Validate that an implementation is provided for the interaction
    *   2. Validate that the return type the interaction matches the expectations from the annotation
    *   3. Check if all the required events are provided
    *   4. Check if all ingredients are provided
    *   5. Check if the provided ingredients are of the same type as the expected ingredients
    */
  def postCompileValidations(compiledRecipe: CompiledRecipe,
                             validationSettings: RecipeValidationSettings): CompiledRecipe = {

    // TODO don't use a mutable list but instead a more functional solutions such as folding or a writer monad
    val postCompileValidationErrors = mutable.ListBuffer.empty[String]

    postCompileValidationErrors ++= validateInteractionIngredients(compiledRecipe)
    postCompileValidationErrors ++= validateInteractions(compiledRecipe)

    if (!validationSettings.allowCycles)
      postCompileValidationErrors ++= validateNoCycles(compiledRecipe)

    if (!validationSettings.allowDisconnectedness && !compiledRecipe.petriNet.isConnected())
      postCompileValidationErrors += "The petrinet topology is not completely connected"

    if (!validationSettings.allowNonExecutableInteractions)
      postCompileValidationErrors ++= validateAllInteractionsExecutable(compiledRecipe)

    compiledRecipe.copy(
      validationErrors = compiledRecipe.validationErrors ++ postCompileValidationErrors)
  }
}
