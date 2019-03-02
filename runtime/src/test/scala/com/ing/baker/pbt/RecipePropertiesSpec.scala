package com.ing.baker.pbt

import java.io.{File, PrintWriter}

import com.ing.baker.compiler.RecipeCompiler
import com.ing.baker.il.{CompiledRecipe, ValidationSettings}
import com.ing.baker.recipe.javadsl
import com.ing.baker.recipe.javadsl.{Event, Ingredient, Interaction, Recipe}
import org.scalacheck.Prop.forAll
import org.scalacheck.Test.Parameters.defaultVerbose
import org.scalacheck._
import org.scalatest.FunSuite
import org.scalatest.prop.Checkers

import scala.annotation.tailrec
import scala.util.Random

class RecipePropertiesSpec extends FunSuite with Checkers {

  import RecipePropertiesSpec._

  test("Baker can compile any valid recipe") {
    val prop = forAll(recipeGen) { recipe =>
      val validations = ValidationSettings(allowCycles = false, allowNonExecutableInteractions = false)

      val compiledRecipe = RecipeCompiler.compileRecipe(recipe, validations)

      if (compiledRecipe.validationErrors.nonEmpty) {
        logRecipeStats(recipe)
        logCompiledRecipeStats(compiledRecipe)
        dumpToFile(s"visualRecipe-${compiledRecipe.name}", compiledRecipe.getRecipeVisualization)
        dumpToFile(s"petrinet-${compiledRecipe.name}", compiledRecipe.getPetriNetVisualization)
      }

      // assertion of the result
      compiledRecipe.validationErrors.isEmpty
    }

    check(prop, defaultVerbose.withMinSuccessfulTests(100))
  }

  ignore("Baker produces all possible ingredients if all sensory events are fired at least once") {
//    implicit val actorSystem = ActorSystem("pbt-actor-system")
//    implicit val duration = FiniteDuration(1, "seconds")
//
//    val prop = forAll(recipeGen) { recipe =>
//
//      val validations = ValidationSettings(allowCycles = false, allowNonExecutableInteractions = false)
//      val compiledRecipe = RecipeCompiler.compileRecipe(recipe, validations)
//
//      logRecipeStats(recipe)
//      logCompiledRecipeStats(compiledRecipe)
//      dumpToFile(s"visualRecipe-${compiledRecipe.name}", compiledRecipe.getRecipeVisualization)
//
//      var alreadyFiredEvents: Set[RuntimeEvent] = Set.empty
//      val petriNetInteractionMock: InteractionTransition => ProcessState => RuntimeEvent = { interaction =>
//        _ =>
//          val outputEvent = interaction.providesType match {
//            case petrinet.FiresOneOfEvents(events, _) =>
//              // Do not fire events again that were fired before
//              val filteredEvents = events.filterNot(eventType => alreadyFiredEvents.map(_.name).contains(eventType.name))
//              sample(Gen.oneOf(filteredEvents).map(e => RuntimeEvent(e.name, ingredientValuesFrom[IngredientType](e.ingredientTypes, _.name))))
//            case petrinet.ProvidesIngredient(ingredient) =>
//              RuntimeEvent(sample(nameGen), ingredientValuesFrom[IngredientType](Seq(ingredient), _.name))
//            case petrinet.ProvidesNothing =>
//              fail("ProvidesNothing type of interaction should not be hit")
//          }
//          println(s"Inside interaction: ${interaction.interactionName}. Firing event ${outputEvent.name}")
//          alreadyFiredEvents += outputEvent
//          outputEvent
//      }
//
//      val baker = new Baker(compiledRecipe, petriNetInteractionMock)
//      val processId = UUID.randomUUID().toString
//      baker.bake(processId)
//
//      val allIngredients = compiledRecipe.ingredients.keySet
//      var counter: Int = 1
//
//      // We need to fire all sensory events multiple times so that we have a chance of traversing all the paths and produce all possible ingredients
//      // 20 times is just a random pick, we need to find a way to make this smarter
//      while(counter <= 20 && !allIngredients.equals(baker.getIngredients(processId).keys)) {
//        println("******** Firing all sensory events. Counter: " + counter)
//        compiledRecipe.sensoryEvents foreach { event =>
//          println(s"Handling sensory event: ${event.name}")
//          val runtimeEvent = RuntimeEvent(event.name, ingredientValuesFrom[IngredientType](event.ingredientTypes, _.name))
//          baker.processEvent(processId, runtimeEvent)
//          alreadyFiredEvents += runtimeEvent
//        }
//        counter += 1
//      }
//
//      // Check this visual state to see what is produced and what is not
//      dumpToFile(s"visualRecipeState-${compiledRecipe.name}", baker.getVisualState(processId))
//
//      allIngredients equals baker.getIngredients(processId).keys
//    }
//
//    check(prop, defaultVerbose.withMinSuccessfulTests(1))
  }

}

object RecipePropertiesSpec {

  val maxNrOfIngredientsPerEvent = 3
  val maxNrOfOutputEventsPerInteraction = 3
  val maxNrOfIngredientsToConsume = 10
  val maxNrOfPreconditionEvents = 3
  val recipeVisualizationOutputPath: String = System.getProperty("java.io.tmpdir")

  val nameGen: Gen[String] = Gen.listOfN(8, Gen.alphaNumChar).map(_.mkString)

  val ingredientGen: Gen[Ingredient] = for {
    name <- nameGen
  } yield Ingredient[String](name)

  val eventGen: Gen[Event] = for {
    name <- nameGen
    nrOfIngredients <- Gen.frequency(
      1 -> Gen.const(0),
      10 -> Gen.choose(1, maxNrOfIngredientsPerEvent)
    )
    providedIngredients <- Gen.listOfN(nrOfIngredients, ingredientGen)
  } yield Event(name, providedIngredients, None)

  val interactionOutputGen: Gen[Seq[Event]] = for {
    nrOfEvents <- Gen.choose(0, maxNrOfOutputEventsPerInteraction)
    events <- Gen.listOfN(nrOfEvents, eventGen)
  } yield events

  val recipeGen: Gen[Recipe] = for {
    name <- nameGen
    sensoryEvents <- Gen.listOf(eventGen) suchThat (_.nonEmpty)
    interactions <- interactionsGen(sensoryEvents) suchThat (_.nonEmpty)
  } yield Recipe(name)
    //turn the lists into var args
    .withSensoryEvents(sensoryEvents.toSet)
    .withInteractions(interactions.toList: _*)

  def interactionsGen(events: Iterable[javadsl.Event]): Gen[Set[Interaction]] = Gen.const(getInteractions(events))

  def getInteractions(sensoryEvents: Iterable[javadsl.Event]): Set[Interaction] = {
    @tailrec def interaction(ingredients: Set[javadsl.Ingredient], events: Set[javadsl.Event], acc: Set[Interaction]): Set[Interaction] = ingredients match {
      case _ingredients if _ingredients.isEmpty => acc
      case ingredientsLeft =>
        val (andPreconditionEvents, orPreconditionEvents) = getPreconditionEvents(events)

        // Sometimes 0 number of ingredients is possible if this interaction has some precondition events
        val minNrOfIngredients =
          if (andPreconditionEvents.size + orPreconditionEvents.size > 0) 0
          else 1

        val nrOfIngredientsToConsume = ingredientsLeft.size min sample(Gen.choose(minNrOfIngredients, maxNrOfIngredientsToConsume))
        val consumedIngredients = Random.shuffle(ingredientsLeft).take(nrOfIngredientsToConsume)

        // Sometimes ingredients should be reused by multiple interactions, so randomizing this behaviour
        val ingredientsToRemove =
          if (Random.nextInt(3) == 0) sample(Gen.someOf(consumedIngredients)).toSet
          else consumedIngredients

        val remainingIngredients = ingredients.diff(ingredientsToRemove)

        val (interactionDescriptor, outputEvents) = getInteractionDescriptor(consumedIngredients, andPreconditionEvents, orPreconditionEvents)

        if (remainingIngredients.isEmpty)
        //those are the last ingredients because the diff is an empty list, so nothing left to weave
          acc + interactionDescriptor
        else
          interaction(
            remainingIngredients ++ getIngredientsFrom(outputEvents),
            events ++ outputEvents,
            acc + interactionDescriptor)
    }

    val ingredients = getIngredientsFrom(sensoryEvents)
    interaction(ingredients, sensoryEvents.toSet, Set.empty)
  }

  /**
    * generates an interactionDescriptor using all the given ingredients, with ProvidesIngredient or FiresOneOfEvents outputs.
    * Also uses the given preconditionEvents as AND and OR preconditions.
    *
    * @param ingredients input ingredients set
    * @return Tuple3(interactionDescriptor, outputIngredients, outputEvents)
    */
  def getInteractionDescriptor(ingredients: Set[javadsl.Ingredient], andPreconditionEvents: Set[javadsl.Event], orPreconditionEvents: Set[javadsl.Event]): (Interaction, Set[javadsl.Event]) = {
    //each interaction fires a single event
    val events = sample(interactionOutputGen)

    val interactionDescriptor = Interaction(sample(nameGen), ingredients.toSeq, events)
      .withRequiredEvents(andPreconditionEvents)
      .withRequiredOneOfEvents(orPreconditionEvents)

    (interactionDescriptor, events.toSet)
  }

  /**
    * Randomly produce precondition events as a subset of given events
    *
    * @param events events set
    * @return Tuple2(andPreconditionEvents, orPreconditionEvents)
    */
  def getPreconditionEvents(events: Set[javadsl.Event]): (Set[javadsl.Event], Set[javadsl.Event]) = {
    val nrOfAndPreconditionEvents = sample(Gen.chooseNum(0, maxNrOfPreconditionEvents))
    val nrOfOrPreconditionEvents = sample(Gen.chooseNum(0, maxNrOfPreconditionEvents))

    val andPreconditionEvents: Set[javadsl.Event] = Random.shuffle(events).take(nrOfAndPreconditionEvents)
    val orPreconditionEvents: Set[javadsl.Event] = {
      val pickedEvents = Random.shuffle(events -- andPreconditionEvents).take(nrOfOrPreconditionEvents)
      if (pickedEvents.size < 2) Set.empty
      else pickedEvents
    }

    (andPreconditionEvents, orPreconditionEvents)
  }

  def getIngredientsFrom(events: Iterable[javadsl.Event]): Set[javadsl.Ingredient] = events.flatMap(_.providedIngredients).toSet

  def ingredientValuesFrom[T](ingredients: Seq[T], nameExtractor: T => String): Map[String, Any] = ingredients map (t => nameExtractor(t) -> "") toMap

  /**
    * Recursively check until there's a sample value is returned
    *
    * @return sample value of the generator
    */
  @tailrec def sample[T](gen: Gen[T]): T = gen.sample match {
    case Some(value) => value
    case None => sample(gen)
  }

  def logRecipeStats(recipe: Recipe): Unit = println(s"\n" +
    s"Generated recipe ::: " +
    s"name: ${recipe.name} " +
    s"nrOfSensoryEvents: ${recipe.sensoryEvents.size} " +
    s"nrOfInteractions: ${recipe.interactions.size} " +
    s"")

  def logCompiledRecipeStats(compiledRecipe: CompiledRecipe): Unit = {
    println(s"Compiled recipe ::: " +
      s"name: ${compiledRecipe.name} " +
      s"nrOfAllIngredients: ${compiledRecipe.allIngredients.size} " +
      s"nrOfSensoryEvents: ${compiledRecipe.sensoryEvents.size} " +
      s"nrOfInteractionEvents: ${compiledRecipe.interactionEvents.size} " +
      s"nrOfInteractions: ${compiledRecipe.interactionTransitions.size} " +
      s"")
    if (compiledRecipe.validationErrors.nonEmpty) println(s"***VALIDATION ERRORS: ${compiledRecipe.validationErrors.mkString("\n")}")
  }

  private def dumpToFile(name: String, data: String): Unit = {
    val fileName =
      if (recipeVisualizationOutputPath endsWith "/") s"$recipeVisualizationOutputPath$name.dot"
      else s"$recipeVisualizationOutputPath/$name.dot"

    val outFile = new File(fileName)
    val writer = new PrintWriter(outFile)

    try {
      writer.write(data)
      println(s"Dumped data with ${data.length} length into $fileName \n")
    } finally {
      writer.close()
    }
  }

}
