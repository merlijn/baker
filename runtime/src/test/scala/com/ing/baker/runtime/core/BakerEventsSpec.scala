package com.ing.baker.runtime.core

import java.util.UUID

import akka.actor.ActorRef
import akka.persistence.inmemory.extension.{InMemoryJournalStorage, StorageExtension}
import akka.testkit.TestProbe
import com.ing.baker._
import com.ing.baker.recipe.TestRecipe._
import com.ing.baker.recipe.javadsl.{InteractionFailureStrategy, Recipe}
import com.ing.baker.runtime.core.events.RejectReason._
import com.ing.baker.runtime.core.events._
import com.ing.baker.types.PrimitiveValue
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.language.postfixOps


object BakerEventsSpec {

  val log = LoggerFactory.getLogger(classOf[BakerEventsSpec])

  def listenerFunction(probe: ActorRef, logEvents: Boolean = false): PartialFunction[BakerEvent, Unit] = {
    case event: BakerEvent =>
      if (logEvents) {
        log.info("Listener consumed event {}", event)
      }
      probe ! event
  }

  def expectMsgInAnyOrderPF[Out](testProbe: TestProbe, pfs: PartialFunction[Any, Out]*): Unit = {
    if (pfs.nonEmpty) {
      val total = pfs.reduce((a, b) ⇒ a.orElse(b))
      testProbe.expectMsgPF() {
        case msg@_ if total.isDefinedAt(msg) ⇒
          val index = pfs.indexWhere(pf ⇒ pf.isDefinedAt(msg))
          val pfn = pfs(index)
          pfn(msg)
          expectMsgInAnyOrderPF[Out](testProbe, pfs.take(index) ++ pfs.drop(index + 1): _*)
      }
    }
  }

  // TODO this is a copy of TestRecipe.getRecipe(..) with 1 difference, there is a limit on the initialEvent, why?
  protected def getRecipe(recipeName: String): Recipe =
    Recipe(recipeName)
      .withInteractions(
        interactionOne
          .withEventOutputTransformer(interactionOneSuccessful, Map("interactionOneOriginalIngredient" -> "interactionOneIngredient"))
          .withFailureStrategy(InteractionFailureStrategy.RetryWithIncrementalBackoff(initialDelay = 10 millisecond, maximumRetries = 3)),
        interactionTwo
          .withOverriddenIngredientName("initialIngredientOld", "initialIngredient"),
        interactionThree
          .withMaximumInteractionCount(1),
        interactionFour
          .withRequiredEvents(Set(secondEvent, eventFromInteractionTwo)),
        interactionFive,
        interactionSix,
        providesNothingInteraction,
        interactionNine
      )
      .withSensoryEvents(Set(
        initialEvent.withMaxFiringLimit(1),
        initialEventExtendedName,
        secondEvent,
        notUsedSensoryEvent))

}

class BakerEventsSpec extends BakerRuntimeTestBase {

  import BakerEventsSpec._

  override def actorSystemName = "BakerEventsSpec"

  val log = LoggerFactory.getLogger(classOf[BakerEventsSpec])

  val eventReceiveTimeout = 1 seconds

  before {
    resetMocks
    setupMockResponse()

    // Clean inmemory-journal before each test
    val tp = TestProbe()
    tp.send(StorageExtension(defaultActorSystem).journalStorage, InMemoryJournalStorage.ClearJournal)
    tp.expectMsg(akka.actor.Status.Success(""))
  }

  "Baker" should {

    "notify ProcessCreated/EventReceived/InteractionStarted/InteractionCompleted events with correct timestamps" in {
      val recipeName = "EventReceivedEventRecipe"
      val processId = UUID.randomUUID().toString
      val (baker, recipeId) = setupBakerWithRecipe(getRecipe(recipeName), mockImplementations)

      val listenerProbe = TestProbe()

      baker.registerEventListener(listenerFunction(listenerProbe.ref))

      baker.createProcess(recipeId, processId)
      baker.fireEvent(processId, InitialEvent(initialIngredientValue), Some("someId"))

      // TODO check the order of the timestamps later
      expectMsgInAnyOrderPF(listenerProbe,
        { case msg@ProcessCreated(_, `recipeId`, `recipeName`, `processId`) => msg },
        { case msg@EventReceived(_, _, _, `processId`, Some("someId"), ProcessEvent("InitialEvent", Seq(Tuple2("initialIngredient", PrimitiveValue(`initialIngredientValue`))))) => msg },
        { case msg@InteractionStarted(_, _, _, `processId`, "InteractionNine") => msg },
        { case msg@InteractionStarted(_, _, _, `processId`, "InteractionOne") => msg },
        { case msg@InteractionStarted(_, _, _, `processId`, "InteractionTwo") => msg },
        { case msg@InteractionStarted(_, _, _, `processId`, "InteractionThree") => msg },
        { case msg@InteractionStarted(_, _, _, `processId`, "ProvidesNothingInteraction") => msg },
        { case msg@InteractionCompleted(_, _, _, _, `processId`, "InteractionOne", Some(ProcessEvent("InteractionOneSuccessful", Seq(Tuple2("interactionOneIngredient",PrimitiveValue("interactionOneIngredient")))))) => msg },
        { case msg@InteractionCompleted(_, _, _, _, `processId`, "InteractionTwo", Some(ProcessEvent("EventFromInteractionTwo", Seq(Tuple2("interactionTwoIngredient", PrimitiveValue("interactionTwoIngredient")))))) => msg },
        { case msg@InteractionCompleted(_, _, _, _, `processId`, "InteractionThree", Some(ProcessEvent("InteractionThreeSuccessful", Seq(Tuple2("interactionThreeIngredient", PrimitiveValue("interactionThreeIngredient")))))) => msg },
        { case msg@InteractionCompleted(_, _, _, _, `processId`, "ProvidesNothingInteraction", None) => msg },
        { case msg@InteractionCompleted(_, _, _, _, `processId`, "InteractionNine", Some(ProcessEvent("InteractionNineSuccessful", Seq(Tuple2("interactionNineIngredient", PrimitiveValue("interactionNineIngredient")))))) => msg }
      )

      listenerProbe.expectNoMessage(eventReceiveTimeout)
    }

    "notify EventRejected event with InvalidEvent reason" in {
      val recipeName = "EventRejectedEventRecipe"
      val processId = UUID.randomUUID().toString
      val (baker, recipeId) = setupBakerWithRecipe(getRecipe(recipeName), mockImplementations)

      val listenerProbe = TestProbe()

      baker.registerEventListener(listenerFunction(listenerProbe.ref))

      baker.createProcess(recipeId, processId)

      // We used async function here because ThirdEvent is not part of the recipe and throws exception
      baker.fireEventAsync(processId, ThirdEvent(), Some("someId"))

      listenerProbe.fishForSpecificMessage(eventReceiveTimeout) {
        case msg@EventRejected(_, `processId`, Some("someId"), ProcessEvent("ThirdEvent", Seq()), InvalidEvent) => msg
      }

      listenerProbe.expectNoMessage(eventReceiveTimeout)
    }

    "notify EventRejected event with AlreadyReceived reason" in {
      val recipeName = "AlreadyReceivedRecipe"
      val processId = UUID.randomUUID().toString
      val (baker, recipeId) = setupBakerWithRecipe(getRecipe(recipeName), mockImplementations)

      val listenerProbe = TestProbe()

      baker.registerEventListener(listenerFunction(listenerProbe.ref))

      baker.createProcess(recipeId, processId)
      baker.fireEvent(processId, InitialEvent(initialIngredientValue), Some("someId"))
      baker.fireEvent(processId, InitialEvent(initialIngredientValue), Some("someId")) // Same correlationId cannot be used twice

      listenerProbe.fishForSpecificMessage(eventReceiveTimeout) {
        case msg@EventRejected(_, `processId`, Some("someId"), ProcessEvent("InitialEvent", Seq(Tuple2("initialIngredient", PrimitiveValue(`initialIngredientValue`)))), AlreadyReceived) => msg
      }

      listenerProbe.expectNoMessage(eventReceiveTimeout)
    }

    "notify EventRejected event with FiringLimitMet reason" in {
      val recipeName = "FiringLimitMetRecipe"
      val processId = UUID.randomUUID().toString
      val (baker, recipeId) = setupBakerWithRecipe(getRecipe(recipeName), mockImplementations)

      val listenerProbe = TestProbe()

      baker.registerEventListener(listenerFunction(listenerProbe.ref))

      baker.createProcess(recipeId, processId)
      baker.fireEvent(processId, InitialEvent(initialIngredientValue))
      baker.fireEvent(processId, InitialEvent(initialIngredientValue)) // Firing limit is set to 1 in the recipe

      listenerProbe.fishForSpecificMessage(eventReceiveTimeout) {
        case msg@EventRejected(_, `processId`, None, ProcessEvent("InitialEvent", Seq(Tuple2("initialIngredient", PrimitiveValue(`initialIngredientValue`)))), FiringLimitMet) => msg
      }

      listenerProbe.expectNoMessage(eventReceiveTimeout)
    }

    "notify EventRejected event with ReceivePeriodExpired reason" in {
      val recipeName = "ReceivePeriodExpiredRecipe"
      val processId = UUID.randomUUID().toString
      val (baker, recipeId) = setupBakerWithRecipe(getRecipe(recipeName).withEventReceivePeriod(eventReceiveTimeout), mockImplementations)

      val listenerProbe = TestProbe()

      baker.registerEventListener(listenerFunction(listenerProbe.ref))

      baker.createProcess(recipeId, processId)

      Thread.sleep(eventReceiveTimeout.toMillis)

      baker.fireEvent(processId, InitialEvent(initialIngredientValue), Some("someId"))

      listenerProbe.fishForSpecificMessage(eventReceiveTimeout) {
        case msg@EventRejected(_, `processId`, Some("someId"), ProcessEvent("InitialEvent", Seq(Tuple2("initialIngredient", PrimitiveValue(`initialIngredientValue`)))), ReceivePeriodExpired) => msg
      }

      listenerProbe.expectNoMessage(eventReceiveTimeout)
    }

    "notify EventRejected event with NoSuchProcess reason" in {
      val recipeName = "NoSuchProcessRecipe"
      val processId = UUID.randomUUID().toString
      val (baker, _) = setupBakerWithRecipe(getRecipe(recipeName), mockImplementations)

      val listenerProbe = TestProbe()

      baker.registerEventListener(listenerFunction(listenerProbe.ref))

      // Skipped baking the process here, so the process with processId does not exist

      // use a different processId and use async function because the sync version throws NoSuchProcessException
      baker.fireEventAsync(processId, InitialEvent(initialIngredientValue), Some("someId"))

      listenerProbe.fishForSpecificMessage(eventReceiveTimeout) {
        case msg@EventRejected(_, `processId`, Some("someId"), ProcessEvent("InitialEvent", Seq(Tuple2("initialIngredient", PrimitiveValue(`initialIngredientValue`)))), NoSuchProcess) => msg
      }

      listenerProbe.expectNoMessage(eventReceiveTimeout)
    }

  }
}
