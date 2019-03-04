package com.ing.baker.il

object InteractionFailureStrategyOutcome {

  /**
    * Indicates that this transition should not be retried but other transitions in the petri net still can.
    */
  case object BlockTransition extends InteractionFailureStrategyOutcome

  /**
    * Retries firing the transition after some delay.
    */
  case class RetryWithDelay(delay: Long) extends InteractionFailureStrategyOutcome {
    require(delay > 0, "Delay must be greater then zero")
  }

  case class Continue(eventName: String) extends InteractionFailureStrategyOutcome
}

sealed trait InteractionFailureStrategyOutcome