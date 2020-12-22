//package com.ing.baker.recipe.dsl.examples
//
//import java.util.Optional
//
//import com.ing.baker.recipe.dsl._
//
//import scala.concurrent.duration._
//
//class ComplexObjectIngredient(value: String)
//
//case class CaseClassIngredient(a: Int, b: String)
//
//object TestRecipe {
//
//  case class InitialEvent(initialIngredient: String)
//
//  case class InitialEventExtendedName(initialIngredientExtendedName: String)
//
//  case class SecondEvent()
//
//  case class ThirdEvent()
//
//  case class FourthEvent()
//
//  case class NotUsedSensoryEvent()
//
//  case class Event1FromInteractionSeven(interactionSevenIngredient1: String)
//
//  case class Event2FromInteractionSeven(interactionSevenIngredient2: String)
//
//  case class EmptyEvent()
//
//  case class UnboxedProviderEvent(missingJavaOptional: String, initialIngredient: String, missingScalaOptional: String)
//
//  //Interactions used in the recipe & implementations (we use traits instead of case classes since we use mocks for the real implementations
//
//  case class InteractionOneSuccessful(interactionOneOriginalIngredient: String)
//
//  trait InteractionOne {
//    def apply(processId: String, initialIngredient: String): InteractionOneSuccessful
//  }
//
//  case class EventFromInteractionTwo(interactionTwoIngredient: String)
//
//  trait InteractionTwo {
//    def apply(initialIngredientOld: String): EventFromInteractionTwo
//  }
//
//  case class InteractionThreeSuccessful(interactionThreeIngredient: String)
//
//  trait InteractionThree {
//    def apply(interactionOneIngredient: String, interactionTwoIngredient: String): InteractionThreeSuccessful
//  }
//
//  case class InteractionFourSuccessful(interactionFourIngredient: String)
//
//  trait InteractionFour {
//    def apply(): InteractionFourSuccessful
//  }
//
//  case class InteractionFiveSuccessful(interactionFiveIngredient: String)
//
//  trait InteractionFive {
//    def apply(processId: String, initialIngredient: String, initialIngredientExtendedName: String): InteractionFiveSuccessful
//  }
//
//  case class InteractionSixSuccessful(interactionSixIngredient: String)
//
//  trait InteractionSix {
//    def apply(initialIngredientExtendedName: String): InteractionSixSuccessful
//  }
//
//  trait InteractionSeven {
//    def apply(initialIngredient: String): String
//  }
//
//  trait InteractionEight {
//    def apply(interactionSevenIngredient1: String, interactionSevenIngredient2: String): Unit
//  }
//
//  trait FireTwoEventsInteraction {
//    def apply(initialIngredient: String): Event1FromInteractionSeven
//  }
//
//  trait ProvidesNothingInteraction {
//    def apply(initialIngredient: String): Unit
//  }
//
//  case class InteractionNineSuccessful(interactionNineIngredient: String)
//
//  trait InteractionNine {
//    def apply(processId: String, initialIngredient: String): InteractionNineSuccessful
//  }
//
//  case class ComplexIngredientInteractionSuccessful(complexOjectIngredient: ComplexObjectIngredient)
//
//  trait ComplexIngredientInteraction {
//    def apply(initialIngredient: String): ComplexIngredientInteractionSuccessful
//  }
//
//  case class CaseClassIngredientInteractionSuccessful(caseClassIngredient: CaseClassIngredient)
//
//  trait CaseClassIngredientInteraction {
//    def apply(initialIngredient: String): CaseClassIngredientInteractionSuccessful
//  }
//
//  trait CaseClassIngredientInteraction2 {
//    def apply(caseClassIngredient: CaseClassIngredient): EmptyEvent
//  }
//
//  trait NonMatchingReturnTypeInteraction {
//    def apply(initialIngredient: String): EventFromInteractionTwo
//  }
//
//  trait OptionalIngredientInteraction {
//    def apply(missingJavaOptional: Optional[String],
//              missingJavaOptional2: Optional[Integer],
//              missingScalaOptional: Option[String],
//              missingScalaOptional2: Option[Integer],
//              initialIngredient: String): Unit
//  }
//
//  // --- Ingredients
//
//  val initialIngredientOld = Ingredient[String]("initialIngredientOld")
//  val initialIngredient = Ingredient[String]("initialIngredient")
//  val interactionOneOriginalIngredient = Ingredient[String]("interactionOneOriginalIngredient")
//  val initialIngredientExtendedName = Ingredient[String]("initialIngredientExtendedName")
//  val interactionOneIngredient = Ingredient[String]("interactionOneIngredient")
//  val interactionTwoIngredient = Ingredient[String]("interactionTwoIngredient")
//  val interactionThreeIngredient = Ingredient[String]("interactionThreeIngredient")
//  val interactionFourIngredient = Ingredient[String]("interactionFourIngredient")
//  val interactionFiveIngredient = Ingredient[String]("interactionFiveIngredient")
//  val interactionSixIngredient = Ingredient[String]("interactionSixIngredient")
//  val interactionSevenIngredient1 = Ingredient[String]("interactionSevenIngredient1")
//  val interactionSevenIngredient2 = Ingredient[String]("interactionSevenIngredient2")
//  val interactionNineIngredient = Ingredient[String]("interactionNineIngredient")
//  val complexObjectIngredient = Ingredient[ComplexObjectIngredient]("complexOjectIngredient")
//  val caseClassIngredient = Ingredient[CaseClassIngredient]("caseClassIngredient")
//  val missingJavaOptional = Ingredient[Optional[String]]("missingJavaOptional")
//  val missingJavaOptionalDirectString  = Ingredient[String]("missingJavaOptional")
//  val missingJavaOptional2  = Ingredient[Optional[Int]]("missingJavaOptional2")
//  val missingScalaOptional  = Ingredient[Option[String]]("missingScalaOptional")
//  val missingScalaOptionalDirectString  = Ingredient[String]("missingScalaOptional")
//  val missingScalaOptional2 = Ingredient[Option[Int]]("missingScalaOptional2")
//
//  // --- Interactions
//
//  val interactionOne = Interaction[InteractionOne]
//  val interactionTwo = Interaction[InteractionTwo]
//  val interactionThree = Interaction[InteractionThree]
//  val interactionFour = Interaction[InteractionFour]
//  val interactionFive = Interaction[InteractionFive]
//  val interactionSix = Interaction[InteractionSix]
//  val interactionSeven = Interaction[InteractionSeven]
//  val interactionEight = Interaction[InteractionEight]
//  val interactionNine = Interaction[InteractionNine]
//
//  val fireTwoEventsInteraction = Interaction[FireTwoEventsInteraction]
//  val providesNothingInteraction = Interaction[ProvidesNothingInteraction]
//  val complexIngredientInteraction = Interaction[ComplexIngredientInteraction]
//  val caseClassIngredientInteraction = Interaction[CaseClassIngredientInteraction]
//  val caseClassIngredientInteraction2 = Interaction[CaseClassIngredientInteraction2]
//  val NonMatchingReturnTypeInteraction = Interaction[NonMatchingReturnTypeInteraction]
//  val optionalIngredientInteraction = Interaction[OptionalIngredientInteraction]
//
//  // --- Events
//
//  val initialEvent = Event[InitialEvent]
//  val initialEventExtendedName = Event[InitialEventExtendedName]
//  val secondEvent = Event[SecondEvent]
//  val thirdEvent = Event[ThirdEvent]
//  val fourthEvent = Event[FourthEvent]
//  val notUsedSensoryEvent = Event[NotUsedSensoryEvent]
//  val eventFromInteractionTwo = Event[EventFromInteractionTwo]
//  val event1FromInteractionSeven = Event[Event1FromInteractionSeven]
//  val event2FromInteractionSeven = Event[Event2FromInteractionSeven]
//  val emptyEvent = Event[EmptyEvent]
////  val exhaustedEvent = Event("RetryExhausted", providedIngredients = Seq.empty)
//  val unboxedProviderEvent = Event[UnboxedProviderEvent]
//  val interactionOneSuccessful: Event = Event[InteractionOneSuccessful]
//
//  def getRecipe(recipeName: String): Recipe =
//    Recipe(recipeName)
//      .withInteractions(
//        interactionOne
//          .withEventOutputTransformer(interactionOneSuccessful, Map("interactionOneOriginalIngredient" -> "interactionOneIngredient"))
//          .withFailureStrategy(InteractionFailureStrategy.RetryWithIncrementalBackoff(initialDelay = 10 millisecond, maximumRetries = 3)),
//        interactionTwo
//          .withOverriddenIngredientName("initialIngredientOld", "initialIngredient"),
//        interactionThree
//          .withMaximumInteractionCount(1),
//        interactionFour
//          .copy(requiredEvents = Set(secondEvent.name, eventFromInteractionTwo.name)),
//        interactionFive,
//        interactionSix,
//        providesNothingInteraction,
//        interactionNine
//      )
//      .withSensoryEvents(Set(
//        initialEvent,
//        initialEventExtendedName,
//        secondEvent,
//        notUsedSensoryEvent))
//}
