package com.ing.baker.runtime.actor.recipe_manager

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor
import com.ing.baker.il.CompiledRecipe
import com.ing.baker.runtime.actor.recipe_manager.RecipeManager._
import com.ing.baker.runtime.actor.recipe_manager.RecipeManagerProtocol._
import com.ing.baker.runtime.actor.serialization.BakerProtoMessage

import scala.collection.mutable

object RecipeManager {

  def props() = Props(new RecipeManager)

  //Events
  //When a recipe is added
  case class RecipeAdded(compiledRecipe: CompiledRecipe, timeStamp: Long) extends BakerProtoMessage
}

class RecipeManager extends PersistentActor with ActorLogging {

  val allRecipes: mutable.Map[String, (CompiledRecipe, Long)] = mutable.Map[String, (CompiledRecipe, Long)]()

  private def hasRecipe(recipe: CompiledRecipe): Option[String] =
    allRecipes.collectFirst { case (recipeId, (`recipe`, _)) =>  recipeId}

  private def addRecipe(compiledRecipe: CompiledRecipe, timestamp: Long) =
    allRecipes += (compiledRecipe.recipeId -> (compiledRecipe, timestamp))


  override def receiveCommand: Receive = {
    case AddRecipe(compiledRecipe) =>
      val foundRecipe = hasRecipe(compiledRecipe)
      if (foundRecipe.isEmpty) {
        val timestamp = System.currentTimeMillis()
        persist(RecipeAdded(compiledRecipe, timestamp)) { _ =>
          addRecipe(compiledRecipe, timestamp)
          context.system.eventStream.publish(
            com.ing.baker.runtime.core.events.RecipeAdded(compiledRecipe.name, compiledRecipe.recipeId, timestamp, compiledRecipe))
          sender() ! AddRecipeResponse(compiledRecipe.recipeId)
        }
      }
      else {
        sender() ! AddRecipeResponse(foundRecipe.get)
      }

    case GetRecipe(recipeId: String) =>
      allRecipes.get(recipeId) match {
        case Some((recipe, timestamp)) => sender() ! RecipeFound(recipe, timestamp)
        case None => sender() ! NoRecipeFound(recipeId)
      }

    case GetAllRecipes =>
      sender() ! AllRecipes(allRecipes.map {
        case (recipeId, (recipe, timestamp)) => RecipeInformation(recipe, timestamp)
      }.toSeq)
  }

  override def receiveRecover: Receive = {
    case RecipeAdded(recipe, timeStamp) => addRecipe(recipe, timeStamp)
  }

  override def persistenceId: String = self.path.name
}
