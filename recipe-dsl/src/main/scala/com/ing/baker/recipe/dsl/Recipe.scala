package com.ing.baker.recipe.dsl

import scala.annotation.varargs
import scala.collection.JavaConverters.*
import scala.concurrent.duration
import scala.concurrent.duration.{Duration, FiniteDuration}

case class Recipe(
    name: String,
    interactions: Seq[Interaction] = Seq.empty,
    sensoryEvents: Seq[Event]= Seq.empty,
    defaultFailureStrategy: InteractionFailureStrategy = InteractionFailureStrategy.BlockInteraction,
    eventReceivePeriod: Option[FiniteDuration] = None,
    retentionPeriod: Option[FiniteDuration] = None) {

  /**
    * This constructor is for java usage:
    *
    * Recipe recipe = new Recipe("name").withEvents(...)
    *
    * @param name
    * @return
    */
  def this(name: String) = this(name, Seq.empty, Seq.empty, InteractionFailureStrategy.BlockInteraction, None, None)

  /**
    * Adds the interactions to the recipe.
    *
    * @param newInteractions The interactions to add
    * @return
    */
  def withInteractions(newInteractions: Interaction*): Recipe =
    copy(interactions = interactions ++ newInteractions)

  /**
    * Adds the sensory event to the recipe
    * The firing limit is set to what is given
    * @param eventClass
    * @param maxFiringLimit
    * @return
    */
  def withSensoryEvent(event: Event, maxFiringLimit: Int): Recipe =
    copy(sensoryEvents = sensoryEvents :+ event.copy(maxFiringLimit = Some(maxFiringLimit)))

  /**
    * Adds the sensory events to the recipe with the firing limit set to 1
    *
    * @param eventClasses
    * @return
    */
  def withSensoryEvents(eventClasses: Event*): Recipe =
    copy(sensoryEvents = sensoryEvents ++ eventClasses.map(_.copy(maxFiringLimit = Some(1))))

  /**
    * Adds the sensory event to the recipe with firing limit set to unlimited
    *
    * @param newEvent
    * @return
    */
  def withSensoryEventNoFiringLimit(event: Event): Recipe =
    withSensoryEventsNoFiringLimit(event)

  /**
    * Adds the sensory events to the recipe with firing limit set to unlimited
    *
    * @param eventClasses
    * @return
    */
  @SafeVarargs
  def withSensoryEventsNoFiringLimit(eventClasses: Event*): Recipe =
    copy(sensoryEvents = sensoryEvents ++ eventClasses.map(_.copy(maxFiringLimit = None)))

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
  def withEventReceivePeriod(recivePeriod: java.time.Duration): Recipe =
    copy(eventReceivePeriod = Some(Duration(recivePeriod.toMillis, duration.MILLISECONDS)))

  /**
    * Sets the process retention period. This is the period for which data & history for processes is kept.
    *
    * @param retentionPeriod The retention period.
    * @return
    */
  def withRetentionPeriod(retentionPeriod: java.time.Duration): Recipe =
    copy(retentionPeriod = Some(Duration(retentionPeriod.toMillis, duration.MILLISECONDS)))


  def withEventReceivePeriod(duration: FiniteDuration): Recipe = copy(eventReceivePeriod = Some(duration))

  def withRetentionPeriod(duration: FiniteDuration): Recipe = copy(retentionPeriod = Some(duration))

  def withSensoryEvent(newEvent: Event): Recipe = copy(sensoryEvents = sensoryEvents :+ newEvent)

  def withSensoryEvents(newEvents: Set[Event]): Recipe = copy(sensoryEvents = sensoryEvents ++ newEvents)

}
