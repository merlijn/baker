package com.ing.baker.recipe

import java.util.Optional

import com.ing.baker.recipe.javadsl._

import scala.concurrent.duration._

//By adding the javadsl Ingredient tag the object will be serialized by Kryo
class ComplexObjectIngredient(value: String)

case class CaseClassIngredient(a: Int, b: String)

object TestRecipe {

  //inputIngredients = Seq as used in the recipe
  val initialIngredientOld = Ingredient[String]("initialIngredientOld")
  val initialIngredient = Ingredient[String]("initialIngredient")
  val interactionOneOriginalIngredient = Ingredient[String]("interactionOneOriginalIngredient")
  val initialIngredientExtendedName = Ingredient[String]("initialIngredientExtendedName")
  val interactionOneIngredient = Ingredient[String]("interactionOneIngredient")
  val interactionTwoIngredient = Ingredient[String]("interactionTwoIngredient")
  val interactionThreeIngredient = Ingredient[String]("interactionThreeIngredient")
  val interactionFourIngredient = Ingredient[String]("interactionFourIngredient")
  val interactionFiveIngredient = Ingredient[String]("interactionFiveIngredient")
  val interactionSixIngredient = Ingredient[String]("interactionSixIngredient")
  val interactionSevenIngredient1 = Ingredient[String]("interactionSevenIngredient1")
  val interactionSevenIngredient2 = Ingredient[String]("interactionSevenIngredient2")
  val interactionNineIngredient = Ingredient[String]("interactionNineIngredient")
  val complexObjectIngredient = Ingredient[ComplexObjectIngredient]("complexOjectIngredient")
  val caseClassIngredient = Ingredient[CaseClassIngredient]("caseClassIngredient")
  val missingJavaOptional = Ingredient[Optional[String]]("missingJavaOptional")
  val missingJavaOptionalDirectString  = Ingredient[String]("missingJavaOptional")
  val missingJavaOptional2  = Ingredient[Optional[Int]]("missingJavaOptional2")
  val missingScalaOptional  = Ingredient[Option[String]]("missingScalaOptional")
  val missingScalaOptionalDirectString  = Ingredient[String]("missingScalaOptional")
  val missingScalaOptional2 = Ingredient[Option[Int]]("missingScalaOptional2")

  //Events as used in the recipe & objects used in runtime
  val initialEvent = Event("InitialEvent", Seq(initialIngredient), maxFiringLimit = None)

  case class InitialEvent(initialIngredient: String)

  val initialEventExtendedName = Event("InitialEventExtendedName", initialIngredientExtendedName)

  case class InitialEventExtendedName(initialIngredientExtendedName: String)

  val secondEvent = Event("SecondEvent")

  case class SecondEvent()

  val thirdEvent = Event("ThirdEvent")

  case class ThirdEvent()

  val fourthEvent = Event("FourthEvent")

  case class FourthEvent()

  val notUsedSensoryEvent = Event("NotUsedSensoryEvent")

  case class NotUsedSensoryEvent()

  val eventFromInteractionTwo = Event("EventFromInteractionTwo", interactionTwoIngredient)

  case class EventFromInteractionTwo(interactionTwoIngredient: String)

  val event1FromInteractionSeven = Event("Event1FromInteractionSeven", interactionSevenIngredient1)

  case class Event1FromInteractionSeven(interactionSevenIngredient1: String)

  val event2FromInteractionSeven = Event("Event2FromInteractionSeven", interactionSevenIngredient2)

  case class Event2FromInteractionSeven(interactionSevenIngredient2: String)

  val emptyEvent = Event("EmptyEvent")

  case class EmptyEvent()

  val exhaustedEvent = Event("RetryExhausted")

  val unboxedProviderEvent = Event("UnboxedProviderEvent", missingJavaOptionalDirectString, initialIngredient, missingScalaOptionalDirectString)

  case class UnboxedProviderEvent(missingJavaOptional: String, initialIngredient: String, missingScalaOptional: String)

  case class InteractionOneSuccessful(interactionOneOriginalIngredient: String)

  val interactionOneSuccessful: javadsl.Event = Event[InteractionOneSuccessful]

  //Interactions used in the recipe & implementations (we use traits instead of case classes since we use mocks for the real implementations
  val interactionOne =
    Interaction(
      name = "InteractionOne",
      input = Seq(processId, initialIngredient),
      output = Seq(interactionOneSuccessful))

  trait InteractionOne {
    def name: String = "InteractionOne"

    def apply(processId: String, initialIngredient: String): InteractionOneSuccessful
  }

  val interactionTwo =
    Interaction(
      name = "InteractionTwo",
      input = Seq(initialIngredientOld),
      output = Seq(eventFromInteractionTwo))

  trait InteractionTwo {
    val name: String = "InteractionTwo"

    def apply(initialIngredientOld: String): EventFromInteractionTwo
  }

  case class InteractionThreeSuccessful(interactionThreeIngredient: String)

  val interactionThree =
    Interaction(
      name = "InteractionThree",
      input = Seq(interactionOneIngredient, interactionTwoIngredient),
      output = Seq(Event[InteractionThreeSuccessful]))

  trait InteractionThree {
    val name: String = "InteractionThree"

    def apply(interactionOneIngredient: String, interactionTwoIngredient: String): InteractionThreeSuccessful
  }

  case class InteractionFourSuccessful(interactionFourIngredient: String)

  val interactionFour =
    Interaction(
      name = "InteractionFour",
      input = Seq.empty,
      output = Seq(Event[InteractionFourSuccessful]))

  trait InteractionFour {
    val name: String = "InteractionFour"

    def apply(): InteractionFourSuccessful
  }

  case class InteractionFiveSuccessful(interactionFiveIngredient: String)

  val interactionFive =
    Interaction(
      name = "InteractionFive",
      input = Seq(processId, initialIngredient, initialIngredientExtendedName),
      output = Seq(Event[InteractionFiveSuccessful]))

  trait InteractionFive {
    val name: String = "InteractionFive"

    def apply(processId: String, initialIngredient: String, initialIngredientExtendedName: String): InteractionFiveSuccessful
  }

  case class InteractionSixSuccessful(interactionSixIngredient: String)

  val interactionSix =
    Interaction(
      name = "InteractionSix",
      input = Seq(initialIngredientExtendedName),
      output = Seq(Event[InteractionSixSuccessful]))

  trait InteractionSix {
    val name: String = "InteractionSix"

    def apply(initialIngredientExtendedName: String): InteractionSixSuccessful
  }

  val interactionSeven =
    Interaction(
      name = "InteractionSeven",
      input = Seq(initialIngredient),
      output = Seq(event1FromInteractionSeven, event2FromInteractionSeven))

  trait InteractionSeven {
    val name: String = "InteractionSeven"

    def apply(initialIngredient: String): String
  }

  val interactionEight =
    Interaction(
      name = "InteractionEight",
      input = Seq(interactionSevenIngredient1, interactionSevenIngredient2),
      output = Seq.empty)

  trait InteractionEight {
    val name: String = "InteractionEight"

    def apply(interactionSevenIngredient1: String, interactionSevenIngredient2: String): Unit
  }

  val fireTwoEventsInteraction =
    Interaction(
      name = "fireTwoEventsInteraction",
      input = Seq(initialIngredient),
      output = Seq(eventFromInteractionTwo, event1FromInteractionSeven))

  trait fireTwoEventsInteraction {
    val name: String = "fireTwoEventsInteraction"

    def apply(initialIngredient: String): Event1FromInteractionSeven
  }

  val providesNothingInteraction =
    Interaction(
      name = "ProvidesNothingInteraction",
      input = Seq(initialIngredient),
      output = Seq.empty)

  trait ProvidesNothingInteraction {
    val name: String = "ProvidesNothingInteraction"

    def apply(initialIngredient: String): Unit
  }

  case class InteractionNineSuccessful(interactionNineIngredient: String)

  val interactionNine =
    Interaction(
      name = "InteractionNine",
      input = Seq(processId, initialIngredient),
      output = Seq(Event[InteractionNineSuccessful]))

  trait InteractionNine {
    val name: String = "InteractionNine"

    def apply(processId: String, initialIngredient: String): InteractionNineSuccessful
  }

  case class ComplexIngredientInteractionSuccessful(complexOjectIngredient: ComplexObjectIngredient)

  val complexIngredientInteraction =
    Interaction(
      name = "ComplexIngredientInteraction",
      input = Seq(initialIngredient),
      Seq(Event[ComplexIngredientInteractionSuccessful]))

  trait ComplexIngredientInteraction {
    val name: String = "ComplexIngredientInteraction"

    def apply(initialIngredient: String): ComplexIngredientInteractionSuccessful
  }

  case class CaseClassIngredientInteractionSuccessful(caseClassIngredient: CaseClassIngredient)

  val caseClassIngredientInteraction =
    Interaction(
      name = "CaseClassIngredientInteraction",
      input = Seq(initialIngredient),
      output = Seq(Event[CaseClassIngredientInteractionSuccessful]))

  trait CaseClassIngredientInteraction {
    val name: String = "CaseClassIngredientInteraction"

    def apply(initialIngredient: String): CaseClassIngredientInteractionSuccessful
  }

  val caseClassIngredientInteraction2 =
    Interaction(
      name = "CaseClassIngredientInteraction2",
      input = Seq(caseClassIngredient),
      output = Seq(emptyEvent))

  trait CaseClassIngredientInteraction2 {
    val name: String = "CaseClassIngredientInteraction2"

    def apply(caseClassIngredient: CaseClassIngredient): EmptyEvent
  }

  val NonMatchingReturnTypeInteraction =
    Interaction(
      name="NonMatchingReturnTypeInteraction",
      input = Seq(initialIngredient),
      output = Seq(eventFromInteractionTwo))

  trait NonMatchingReturnTypeInteraction {
    val name: String = "NonMatchingReturnTypeInteraction"

    def apply(initialIngredient: String): EventFromInteractionTwo
  }

  val optionalIngredientInteraction =
    Interaction(
      name = "OptionalIngredientInteraction",
      input = Seq(
        missingJavaOptional,
        missingJavaOptional2,
        missingScalaOptional,
        missingScalaOptional2,
        initialIngredient),
      output = Seq.empty)

  trait OptionalIngredientInteraction {
    val name: String = "OptionalIngredientInteraction"

    def apply(missingJavaOptional: Optional[String],
              missingJavaOptional2: Optional[Integer],
              missingScalaOptional: Option[String],
              missingScalaOptional2: Option[Integer],
              initialIngredient: String)
  }

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
