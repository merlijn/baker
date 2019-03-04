package com.ing.baker.il

import java.util.concurrent.TimeUnit

import com.ing.baker.il.InteractionFailureStrategyOutcome.{BlockTransition, Continue, RetryWithDelay}

import scala.concurrent.duration.Duration

object InteractionFailureStrategy {

  val oneWeekInMillis: Long = Duration.apply(7 , TimeUnit.DAYS).toMillis

  case object BlockInteraction extends InteractionFailureStrategy {
    def apply(n: Int) : InteractionFailureStrategyOutcome = InteractionFailureStrategyOutcome.BlockTransition
  }

  case class FireEventAfterFailure(event: EventDescriptor) extends InteractionFailureStrategy {

    override def apply(n: Int): InteractionFailureStrategyOutcome = Continue(event.name)
  }

  case class RetryWithIncrementalBackoff(initialTimeout: Duration,
                                         backoffFactor: Double,
                                         maximumRetries: Int,
                                         maxTimeBetweenRetries: Option[Duration],
                                         retryExhaustedEvent: Option[EventDescriptor])
    extends InteractionFailureStrategy {
    require(backoffFactor >= 1.0, "backoff factor must be greater or equal to 1.0")
    require(maximumRetries >= 1, "maximum retries must be greater or equal to 1")

    def determineTimeToNextRetry(n: Int): Long = {
      val nextRetry: Long = initialTimeout.toMillis * Math.pow(backoffFactor, n - 1).toLong
      val positiveNextRetry: Long = if (nextRetry > oneWeekInMillis || nextRetry <= 0) oneWeekInMillis else nextRetry

      maxTimeBetweenRetries match {
        case Some(duration) => if (positiveNextRetry > duration.toMillis) duration.toMillis else positiveNextRetry
        case None => positiveNextRetry
      }
    }

    def apply(n: Int): InteractionFailureStrategyOutcome = {
      if (n <= maximumRetries) RetryWithDelay(determineTimeToNextRetry(n))
      else if (retryExhaustedEvent.isDefined) Continue(retryExhaustedEvent.get.name)
      else BlockTransition
    }
  }
}

trait InteractionFailureStrategy {
  def apply(n: Int): InteractionFailureStrategyOutcome
}
