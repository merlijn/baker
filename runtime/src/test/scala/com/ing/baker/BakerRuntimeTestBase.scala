package com.ing.baker

import java.nio.file.Paths
import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.ing.baker.compiler.RecipeCompiler
import com.ing.baker.il.CompiledRecipe
import com.ing.baker.recipe.dsl.examples.CaseClassIngredient
import com.ing.baker.recipe.dsl.examples.TestRecipe.{FireTwoEventsInteraction, _}
import com.ing.baker.recipe.dsl.Recipe
import com.ing.baker.runtime.core.{Baker, ProcessEvent}
import com.ing.baker.types.Value
import com.ing.baker.types.reflect.Reflect
import com.typesafe.config.{Config, ConfigFactory}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest._
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.duration._
import scala.language.postfixOps

trait BakerRuntimeTestBase
  extends WordSpecLike
    with Matchers
    with MockitoSugar
    with BeforeAndAfter
    with BeforeAndAfterAll {

  def actorSystemName: String

  implicit val timeout: FiniteDuration = 10 seconds

  //Values to use for setting and checking the ingredients

  //Default values to be used for the ingredients in the tests
  val initialIngredientValue = "initialIngredient"
  val interactionNineIngredientValue = "interactionNineIngredient"
  val interactionOneOriginalIngredientValue = "interactionOneOriginalIngredient"
  val interactionOneIngredientValue = "interactionOneIngredient"
  val interactionTwoIngredientValue = "interactionTwoIngredient"
  val interactionTwoEventValue = EventFromInteractionTwo(interactionTwoIngredientValue)
  val interactionThreeIngredientValue = "interactionThreeIngredient"
  val interactionFourIngredientValue = "interactionFourIngredient"
  val interactionFiveIngredientValue = "interactionFiveIngredient"
  val interactionSixIngredientValue = "interactionSixIngredient"
  val caseClassIngredientValue = CaseClassIngredient(5, "this is a case class test")
  val errorMessage = "This is the error message"

  def ingredientMap(entries: (String, Any)*): Map[String, Value] =
    entries.map { case (name, obj) => name -> Reflect.toValue(obj) }.toMap

  def eventList(events: Any*): Seq[ProcessEvent]= events.map(e => ProcessEvent.of((e)))

  //Can be used to check the state after firing the initialEvent
  val afterInitialState = ingredientMap(
    "initialIngredient" -> initialIngredientValue,
    "interactionNineIngredient" -> interactionNineIngredientValue,
    "interactionOneIngredient" -> interactionOneIngredientValue,
    "interactionTwoIngredient" -> interactionTwoIngredientValue,
    "interactionThreeIngredient" -> interactionThreeIngredientValue
  )

  //Can be used to check the state after firing the initialEvent and SecondEvent
  val finalState = ingredientMap(
    "initialIngredient" -> initialIngredientValue,
    "interactionNineIngredient" -> interactionNineIngredientValue,
    "interactionOneIngredient" -> interactionOneIngredientValue,
    "interactionTwoIngredient" -> interactionTwoIngredientValue,
    "interactionThreeIngredient" -> interactionThreeIngredientValue,
    "interactionFourIngredient" -> interactionFourIngredientValue
  )

  val testInteractionOneMock: InteractionOne = mock[InteractionOne]
  val testInteractionTwoMock: InteractionTwo = mock[InteractionTwo]
  val testInteractionThreeMock: InteractionThree = mock[InteractionThree]
  val testInteractionFourMock: InteractionFour = mock[InteractionFour]
  val testInteractionFiveMock: InteractionFive = mock[InteractionFive]
  val testInteractionSixMock: InteractionSix = mock[InteractionSix]
  val testFireTwoEventsInteractionMock: FireTwoEventsInteraction = mock[FireTwoEventsInteraction]
  val testComplexIngredientInteractionMock: ComplexIngredientInteraction = mock[ComplexIngredientInteraction]
  val testCaseClassIngredientInteractionMock: CaseClassIngredientInteraction = mock[CaseClassIngredientInteraction]
  val testCaseClassIngredientInteraction2Mock: CaseClassIngredientInteraction2 = mock[CaseClassIngredientInteraction2]
  val testNonMatchingReturnTypeInteractionMock: NonMatchingReturnTypeInteraction = mock[NonMatchingReturnTypeInteraction]
  val testInteractionNineMock: InteractionNine = mock[InteractionNine]
  val testOptionalIngredientInteractionMock: OptionalIngredientInteraction = mock[OptionalIngredientInteraction]
  val testProvidesNothingInteractionMock: ProvidesNothingInteraction = mock[ProvidesNothingInteraction]

  val mockImplementations: Seq[AnyRef] =
    Seq(
      testInteractionOneMock,
      testInteractionTwoMock,
      testInteractionThreeMock,
      testInteractionFourMock,
      testInteractionFiveMock,
      testInteractionSixMock,
      testFireTwoEventsInteractionMock,
      testComplexIngredientInteractionMock,
      testCaseClassIngredientInteractionMock,
      testCaseClassIngredientInteraction2Mock,
      testNonMatchingReturnTypeInteractionMock,
      testInteractionNineMock,
      testOptionalIngredientInteractionMock,
      testProvidesNothingInteractionMock)

  def writeRecipeToSVGFile(recipe: CompiledRecipe) = {

    import guru.nidi.graphviz.engine.{Format, Graphviz}
    import guru.nidi.graphviz.parse.Parser

    val g = Parser.read(recipe.getRecipeVisualization)

    Graphviz.fromGraph(g).render(Format.SVG).toFile(Paths.get(recipe.name).toFile)
  }

  def localLevelDBConfig(actorSystemName: String,
                                   journalInitializeTimeout: FiniteDuration = 10 seconds,
                                   journalPath: String = "target/journal",
                                   snapshotsPath: String = "target/snapshots"): Config =
    ConfigFactory.parseString(
      s"""
         |
         |
         |akka {
         |
         |  actor {
         |    provider = "akka.actor.LocalActorRefProvider"
         |    allow-java-serialization = off
         |    serialize-messages = on
         |    serialize-creators = off
         |  }
         |
         |  persistence {
         |     journal.plugin = "akka.persistence.journal.leveldb"
         |     journal.leveldb.dir = "$journalPath"
         |
         |     snapshot-store.plugin = "akka.persistence.snapshot-store.local"
         |     snapshot-store.local.dir = "$snapshotsPath"
         |
         |     auto-start-snapshot-stores = [ "akka.persistence.snapshot-store.local"]
         |     auto-start-journals = [ "akka.persistence.journal.leveldb" ]
         |
         |     journal.leveldb.native = off
         |  }
         |
         |  loggers = ["akka.event.slf4j.Slf4jLogger"]
         |  loglevel = "DEBUG"
         |  logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"
         |}
         |
         |baker {
         |  actor.read-journal-plugin = "akka.persistence.query.journal.leveldb"
         |  journal-initialize-timeout = $journalInitializeTimeout
         |}
         |
       |logging.root.level = DEBUG
    """.stripMargin)

  def clusterLevelDBConfig(actorSystemName: String,
                                     port: Int,
                                     journalInitializeTimeout: FiniteDuration = 10 seconds,
                                     journalPath: String = "target/journal",
                                     snapshotsPath: String = "target/snapshots"): Config =

    ConfigFactory.parseString(
    s"""
       |akka {
       |
       |  actor.provider = "akka.cluster.ClusterActorRefProvider"
       |
       |  remote {
       |    artery {
       |      canonical.hostname = "localhost"
       |      canonical.port = $port
       |    }
       |  }
       |
       |  cluster.seed-nodes = ["akka://$actorSystemName@localhost:$port"]
       |}
    """.stripMargin).withFallback(localLevelDBConfig(actorSystemName, journalInitializeTimeout, journalPath, snapshotsPath))

  implicit val defaultActorSystem = ActorSystem(actorSystemName)

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(defaultActorSystem)
  }

  /**
    * Returns a Baker instance that contains a simple recipe that can be used in tests
    * It als sets mocks that return happy flow responses for the interactions
    *
    * This recipe contains: See TestRecipe.png for a visualization
    *
    * @param recipeName A unique name that is needed for the recipe to insure that the tests do not interfere with each other
    * @return
    */
  def setupBakerWithRecipe(recipeName: String, appendUUIDToTheRecipeName: Boolean = true)
                                    (implicit actorSystem: ActorSystem): (Baker, String) = {

    val newRecipeName = if (appendUUIDToTheRecipeName) s"$recipeName-${UUID.randomUUID().toString}" else recipeName
    val recipe = getRecipe(newRecipeName)
    setupMockResponse()

    setupBakerWithRecipe(recipe, mockImplementations)(actorSystem)
  }

  def setupBakerWithRecipe(recipe: Recipe, implementations: Seq[AnyRef])
                                    (implicit actorSystem: ActorSystem): (Baker, String) = {

    val baker = new Baker()(actorSystem)
    baker.addImplementationMethods(implementations)
    val recipeId = baker.addRecipe(RecipeCompiler.compileRecipe(recipe))
    (baker, recipeId)
  }

  def setupBakerWithNoRecipe()(implicit actorSystem: ActorSystem): Baker = {
    setupMockResponse()
    val baker = new Baker()(actorSystem)
    baker.addImplementationMethods(mockImplementations)
    baker
  }

  def setupMockResponse(): Unit = {
    when(testInteractionOneMock.apply(anyString(), anyString())).thenReturn(InteractionOneSuccessful(interactionOneIngredientValue))
    when(testInteractionTwoMock.apply(anyString())).thenReturn(interactionTwoEventValue)
    when(testInteractionThreeMock.apply(anyString(), anyString())).thenReturn(InteractionThreeSuccessful(interactionThreeIngredientValue))
    when(testInteractionFourMock.apply()).thenReturn(InteractionFourSuccessful(interactionFourIngredientValue))
    when(testInteractionFiveMock.apply(anyString(), anyString(), anyString())).thenReturn(InteractionFiveSuccessful(interactionFiveIngredientValue))
    when(testInteractionSixMock.apply(anyString())).thenReturn(InteractionSixSuccessful(interactionSixIngredientValue))
    when(testInteractionNineMock.apply(anyString(), anyString())).thenReturn(InteractionNineSuccessful(interactionNineIngredientValue))
  }

  def timeBlockInMilliseconds[T](block: => T): Long = {
    val t0 = System.nanoTime()
    block
    val t1 = System.nanoTime()
    val amountOfNanosecondsInOneMillisecond = 1000000
    val milliseconds = (t1 - t0) / amountOfNanosecondsInOneMillisecond

    milliseconds
  }

  def withActorSystem(customActorSystem: ActorSystem)(fn: ActorSystem => Unit) = {

    try {
      fn(customActorSystem)
    }
    finally {
      TestKit.shutdownActorSystem(customActorSystem)
    }
  }

  def resetMocks(): Unit =
    reset(testInteractionOneMock,
      testInteractionTwoMock,
      testInteractionThreeMock,
      testInteractionFourMock,
      testInteractionFiveMock,
      testInteractionSixMock,
      testNonMatchingReturnTypeInteractionMock)

}
