package com.ing.baker.recipe.javadsl

import com.ing.baker.recipe.javadsl

import scala.annotation.varargs
import scala.collection.JavaConverters._
import scala.concurrent.duration
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.language.experimental.macros

object Recipe {
  def apply() : Recipe = macro CommonMacros.recipeImpl

  def apply(name: String): Recipe = {
    Recipe(name, Seq.empty, Seq.empty, InteractionFailureStrategy.BlockInteraction(), None, None)
  }
}

case class Recipe(
    name: String,
    interactions: Seq[Interaction],
    sensoryEvents: Seq[Event],
    defaultFailureStrategy: InteractionFailureStrategy = InteractionFailureStrategy.BlockInteraction(),
    eventReceivePeriod: Option[FiniteDuration] = None,
    retentionPeriod: Option[FiniteDuration] = None) {

  def this(name: String) = this(name, Seq.empty, Seq.empty, InteractionFailureStrategy.BlockInteraction(), None, None)

  def getInteractions: java.util.List[Interaction] = interactions.asJava

  def getEvents: java.util.List[Event] = sensoryEvents.toList.asJava

  /**
    * This adds all interactions of the recipe to this recipe
    * Sensory Events are not added and are expected to be given by the recipe itself
    *
    * @param recipe
    * @return
    */
  def withRecipe(recipe: Recipe) = {
    copy(interactions = interactions ++ recipe.interactions)
  }

  /**
    * Adds the interaction to the recipe.
    *
    * @param newInteraction the interaction to add
    * @return
    */
  def withInteraction(newInteraction: Interaction): Recipe =
    withInteractions(Seq(newInteraction): _*)

  /**
    * Adds the interactions to the recipe.
    *
    * @param newInteractions The interactions to add
    * @return
    */
  @SafeVarargs
  @varargs
  def withInteractions(newInteractions: Interaction*): Recipe =
    copy(interactions = interactions ++ newInteractions)

  /**
    * Adds the sensory event to the recipe
    * The firing limit is set to 1 by default
    * @param newEvent
    * @return
    */
  def withSensoryEvent(newEvent: Class[_]): Recipe =
    withSensoryEvents(newEvent)

  /**
    * Adds the sensory event to the recipe
    * The firing limit is set to what is given
    * @param newEvent
    * @param maxFiringLimit
    * @return
    */
  def withSensoryEvent(newEvent: Class[_], maxFiringLimit: Int): Recipe =
    copy(sensoryEvents = sensoryEvents :+ javadsl.Event.fromClass(newEvent, Some(maxFiringLimit)))

  /**
    * Adds the sensory events to the recipe with the firing limit set to 1
    *
    * @param eventsToAdd
    * @return
    */
  @SafeVarargs
  @varargs
  def withSensoryEvents(eventsToAdd: Class[_]*): Recipe =
    copy(sensoryEvents = sensoryEvents ++ eventsToAdd.map(javadsl.Event.fromClass(_, Some(1))))

  /**
    * Adds the sensory event to the recipe with firing limit set to unlimited
    *
    * @param newEvent
    * @return
    */
  def withSensoryEventNoFiringLimit(newEvent: Class[_]): Recipe =
    withSensoryEventsNoFiringLimit(newEvent)


  /**
    * Adds the sensory events to the recipe with firing limit set to unlimited
    *
    * @param eventsToAdd
    * @return
    */
  @SafeVarargs
  @varargs
  def withSensoryEventsNoFiringLimit(eventsToAdd: Class[_]*): Recipe =
    copy(sensoryEvents = sensoryEvents ++ eventsToAdd.map(javadsl.Event.fromClass(_, None)))

  /**
    * This set the failure strategy as default for this recipe.
    * If a failure strategy is set for the Interaction itself that is taken.
    *
    * @param interactionFailureStrategy The failure strategy to follow
    * @return
    */
  def withDefaultFailureStrategy(interactionFailureStrategy: InteractionFailureStrategy): Recipe =
    copy(defaultFailureStrategy = interactionFailureStrategy)

  /**
    * Sets the event receive period. This is the period for which processes can receive sensory events.
    *
    * @param recivePeriod The period
    * @return
    */
  def withEventReceivePeriod(recivePeriod: java.time.Duration) =
    copy(eventReceivePeriod = Some(Duration(recivePeriod.toMillis, duration.MILLISECONDS)))

  /**
    * Sets the process retention period. This is the period for which data & history for processes is kept.
    *
    * @param retentionPeriod The retention period.
    * @return
    */
  def withRetentionPeriod(retentionPeriod: java.time.Duration) =
    copy(retentionPeriod = Some(Duration(retentionPeriod.toMillis, duration.MILLISECONDS)))


  def withEventReceivePeriod(duration: FiniteDuration): Recipe = copy(eventReceivePeriod = Some(duration))

  def withRetentionPeriod(duration: FiniteDuration): Recipe = copy(retentionPeriod = Some(duration))

  def withSensoryEvent(newEvent: Event): Recipe = copy(sensoryEvents = sensoryEvents :+ newEvent)

  def withSensoryEvents(newEvents: Set[Event]): Recipe = copy(sensoryEvents = sensoryEvents ++ newEvents)

}
