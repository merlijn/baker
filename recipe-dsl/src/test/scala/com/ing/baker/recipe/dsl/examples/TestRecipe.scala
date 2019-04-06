package com.ing.baker.recipe.dsl.examples

import java.util.Optional

import com.ing.baker.recipe.annotations.ProcessId
import com.ing.baker.recipe.dsl._
import javax.inject.Named

import scala.concurrent.duration._

class ComplexObjectIngredient(value: String)

case class CaseClassIngredient(a: Int, b: String)

object TestRecipe {

  case class InitialEvent(initialIngredient: String)

  case class InitialEventExtendedName(initialIngredientExtendedName: String)

  case class SecondEvent()

  case class ThirdEvent()

  case class FourthEvent()

  case class NotUsedSensoryEvent()

  case class Event1FromInteractionSeven(interactionSevenIngredient1: String)

  case class Event2FromInteractionSeven(interactionSevenIngredient2: String)

  case class EmptyEvent()

  case class UnboxedProviderEvent(missingJavaOptional: String, initialIngredient: String, missingScalaOptional: String)

  //Interactions used in the recipe & implementations (we use traits instead of case classes since we use mocks for the real implementations

  case class InteractionOneSuccessful(interactionOneOriginalIngredient: String)

  trait InteractionOne {
    def apply(@ProcessId processId: String, @Named("initialIngredient") initialIngredient: String): InteractionOneSuccessful
  }

  case class EventFromInteractionTwo(interactionTwoIngredient: String)

  trait InteractionTwo {
    def apply(@Named("initialIngredientOld") initialIngredientOld: String): EventFromInteractionTwo
  }

  case class InteractionThreeSuccessful(interactionThreeIngredient: String)

  trait InteractionThree {
    def apply(@Named("interactionOneIngredient") interactionOneIngredient: String, @Named("interactionTwoIngredient") interactionTwoIngredient: String): InteractionThreeSuccessful
  }

  case class InteractionFourSuccessful(interactionFourIngredient: String)

  trait InteractionFour {
    def apply(): InteractionFourSuccessful
  }

  case class InteractionFiveSuccessful(interactionFiveIngredient: String)

  trait InteractionFive {
    def apply(@ProcessId processId: String, @Named("initialIngredient") initialIngredient: String, @Named("initialIngredientExtendedName") initialIngredientExtendedName: String): InteractionFiveSuccessful
  }

  case class InteractionSixSuccessful(interactionSixIngredient: String)

  trait InteractionSix {
    def apply(@Named("initialIngredientExtendedName") initialIngredientExtendedName: String): InteractionSixSuccessful
  }

  trait InteractionSeven {
    def apply(@Named("initialIngredient") initialIngredient: String): String
  }

  trait InteractionEight {
    def apply(@Named("interactionSevenIngredient1") interactionSevenIngredient1: String, @Named("interactionSevenIngredient2") interactionSevenIngredient2: String): Unit
  }

  trait FireTwoEventsInteraction {
    def apply(@Named("initialIngredient") initialIngredient: String): Event1FromInteractionSeven
  }

  trait ProvidesNothingInteraction {
    def apply(@Named("initialIngredient") initialIngredient: String): Unit
  }

  case class InteractionNineSuccessful(interactionNineIngredient: String)

  trait InteractionNine {
    def apply(@ProcessId processId: String, @Named("initialIngredient") initialIngredient: String): InteractionNineSuccessful
  }

  case class ComplexIngredientInteractionSuccessful(complexOjectIngredient: ComplexObjectIngredient)

  trait ComplexIngredientInteraction {
    def apply(@Named("initialIngredient") initialIngredient: String): ComplexIngredientInteractionSuccessful
  }

  case class CaseClassIngredientInteractionSuccessful(caseClassIngredient: CaseClassIngredient)

  trait CaseClassIngredientInteraction {
    def apply(@Named("initialIngredient") initialIngredient: String): CaseClassIngredientInteractionSuccessful
  }

  trait CaseClassIngredientInteraction2 {
    def apply(@Named("caseClassIngredient") caseClassIngredient: CaseClassIngredient): EmptyEvent
  }

  trait NonMatchingReturnTypeInteraction {
    def apply(@Named("initialIngredient") initialIngredient: String): EventFromInteractionTwo
  }

  trait OptionalIngredientInteraction {
    def apply(@Named("missingJavaOptional") missingJavaOptional: Optional[String],
              @Named("missingJavaOptional2") missingJavaOptional2: Optional[Integer],
              @Named("missingScalaOptional") missingScalaOptional: Option[String],
              @Named("missingScalaOptional2") missingScalaOptional2: Option[Integer],
              @Named("initialIngredient") initialIngredient: String): Unit
  }

  // --- Ingredients

  val initialIngredientOld = Ingredient.reflect[String]("initialIngredientOld")
  val initialIngredient = Ingredient.reflect[String]("initialIngredient")
  val interactionOneOriginalIngredient = Ingredient.reflect[String]("interactionOneOriginalIngredient")
  val initialIngredientExtendedName = Ingredient.reflect[String]("initialIngredientExtendedName")
  val interactionOneIngredient = Ingredient.reflect[String]("interactionOneIngredient")
  val interactionTwoIngredient = Ingredient.reflect[String]("interactionTwoIngredient")
  val interactionThreeIngredient = Ingredient.reflect[String]("interactionThreeIngredient")
  val interactionFourIngredient = Ingredient.reflect[String]("interactionFourIngredient")
  val interactionFiveIngredient = Ingredient.reflect[String]("interactionFiveIngredient")
  val interactionSixIngredient = Ingredient.reflect[String]("interactionSixIngredient")
  val interactionSevenIngredient1 = Ingredient.reflect[String]("interactionSevenIngredient1")
  val interactionSevenIngredient2 = Ingredient.reflect[String]("interactionSevenIngredient2")
  val interactionNineIngredient = Ingredient.reflect[String]("interactionNineIngredient")
  val complexObjectIngredient = Ingredient.reflect[ComplexObjectIngredient]("complexOjectIngredient")
  val caseClassIngredient = Ingredient.reflect[CaseClassIngredient]("caseClassIngredient")
  val missingJavaOptional = Ingredient.reflect[Optional[String]]("missingJavaOptional")
  val missingJavaOptionalDirectString  = Ingredient.reflect[String]("missingJavaOptional")
  val missingJavaOptional2  = Ingredient.reflect[Optional[Int]]("missingJavaOptional2")
  val missingScalaOptional  = Ingredient.reflect[Option[String]]("missingScalaOptional")
  val missingScalaOptionalDirectString  = Ingredient.reflect[String]("missingScalaOptional")
  val missingScalaOptional2 = Ingredient.reflect[Option[Int]]("missingScalaOptional2")

  // --- Interactions

  val interactionOne = Interaction.reflect[InteractionOne]
  val interactionTwo = Interaction.reflect[InteractionTwo]
  val interactionThree = Interaction.reflect[InteractionThree]
  val interactionFour = Interaction.reflect[InteractionFour]
  val interactionFive = Interaction.reflect[InteractionFive]
  val interactionSix = Interaction.reflect[InteractionSix]
  val interactionSeven = Interaction.reflect[InteractionSeven]
  val interactionEight = Interaction.reflect[InteractionEight]
  val interactionNine = Interaction.reflect[InteractionNine]

  val fireTwoEventsInteraction = Interaction.reflect[FireTwoEventsInteraction]
  val providesNothingInteraction = Interaction.reflect[ProvidesNothingInteraction]
  val complexIngredientInteraction = Interaction.reflect[ComplexIngredientInteraction]
  val caseClassIngredientInteraction = Interaction.reflect[CaseClassIngredientInteraction]
  val caseClassIngredientInteraction2 = Interaction.reflect[CaseClassIngredientInteraction2]
  val NonMatchingReturnTypeInteraction = Interaction.reflect[NonMatchingReturnTypeInteraction]
  val optionalIngredientInteraction = Interaction.reflect[OptionalIngredientInteraction]

  // --- Events

  val initialEvent = Event("InitialEvent", Seq(initialIngredient), maxFiringLimit = None)
  val initialEventExtendedName = Event("InitialEventExtendedName", Seq(initialIngredientExtendedName))
  val secondEvent = Event("SecondEvent")
  val thirdEvent = Event("ThirdEvent")
  val fourthEvent = Event("FourthEvent")
  val notUsedSensoryEvent = Event("NotUsedSensoryEvent")
  val eventFromInteractionTwo = Event("EventFromInteractionTwo", Seq(interactionTwoIngredient))
  val event1FromInteractionSeven = Event("Event1FromInteractionSeven", Seq(interactionSevenIngredient1))
  val event2FromInteractionSeven = Event("Event2FromInteractionSeven", Seq(interactionSevenIngredient2))
  val emptyEvent = Event("EmptyEvent", providedIngredients = Seq.empty)
  val exhaustedEvent = Event("RetryExhausted", providedIngredients = Seq.empty)
  val unboxedProviderEvent = Event("UnboxedProviderEvent", Seq(missingJavaOptionalDirectString, initialIngredient, missingScalaOptionalDirectString))
  val interactionOneSuccessful: Event = Event.reflect[InteractionOneSuccessful]

  def getRecipe(recipeName: String): Recipe =
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
          .copy(requiredEvents = Set(secondEvent.name, eventFromInteractionTwo.name)),
        interactionFive,
        interactionSix,
        providesNothingInteraction,
        interactionNine
      )
      .withSensoryEvents(Set(
        initialEvent,
        initialEventExtendedName,
        secondEvent,
        notUsedSensoryEvent))
}
