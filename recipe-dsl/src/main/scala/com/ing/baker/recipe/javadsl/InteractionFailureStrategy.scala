package com.ing.baker.recipe.javadsl

import java.util.concurrent.TimeUnit

import scala.annotation.tailrec
import scala.concurrent.duration.{Duration, FiniteDuration, MILLISECONDS}

sealed trait InteractionFailureStrategy

object InteractionFailureStrategy {

  case class BlockInteraction() extends InteractionFailureStrategy

  case class FireEventAfterFailure(eventName: Option[String] = None) extends InteractionFailureStrategy

  object RetryWithIncrementalBackoff {

    def builder() = new RetryWithIncrementalBackoffBuilder()
  }

  case class RetryWithIncrementalBackoff(initialDelay: Duration,
                                         backoffFactor: Double = 2,
                                         maximumRetries: Int,
                                         maxTimeBetweenRetries: Option[Duration] = None,
                                         fireRetryExhaustedEvent: Option[Option[String]] = None) extends InteractionFailureStrategy {

    require(backoffFactor >= 1.0, "backoff factor must be greater or equal to 1.0")
    require(maximumRetries >= 1, "maximum retries must be greater or equal to 1")
  }


  sealed trait Until

  case class UntilDeadline(duration: Duration) extends Until

  case class UntilMaximumRetries(count: Int) extends Until

  case class RetryWithIncrementalBackoffBuilder private(private val initialDelay: Option[Duration],
                                                        private val backoffFactor: Double,
                                                        private val until: Option[Until],
                                                        private val maxTimeBetweenRetries: Option[Duration],
                                                        private val fireRetryExhaustedEvent: Option[Option[String]]) {
    // initialize with defaults
    def this() = this(initialDelay = None, backoffFactor = 2, until = None, maxTimeBetweenRetries = None, fireRetryExhaustedEvent = None)

    def withInitialDelay(initialDelay: FiniteDuration) = copy(initialDelay = Some(initialDelay))

    def withInitialDelay(initialDelay: java.time.Duration) = copy(initialDelay = Some(FiniteDuration(initialDelay.toMillis, TimeUnit.MILLISECONDS)))

    def withBackoffFactor(backoffFactor: Double) = copy(backoffFactor = backoffFactor)

    def withMaximumRetries(count: Int) = copy(until = Some(UntilMaximumRetries(count)))

    def withMaxTimeBetweenRetries(maxTimeBetweenRetries: FiniteDuration) = copy(maxTimeBetweenRetries = Some(maxTimeBetweenRetries))

    def withMaxTimeBetweenRetries(maxTimeBetweenRetries: java.time.Duration) = copy(maxTimeBetweenRetries = Some(FiniteDuration(maxTimeBetweenRetries.toMillis, TimeUnit.MILLISECONDS)))

    def withFireRetryExhaustedEvent(fireRetryExhaustedEvent: String) = copy(fireRetryExhaustedEvent = Some(Some(fireRetryExhaustedEvent)))

    def withFireRetryExhaustedEvent() = copy(fireRetryExhaustedEvent = Some(None))

    def withFireRetryExhaustedEvent(fireRetryExhaustedEvent: Class[_]) = copy(fireRetryExhaustedEvent = Some(Some(fireRetryExhaustedEvent.getName)))

    def withDeadline(duration: FiniteDuration) = copy(until = Some(UntilDeadline(duration)))

    def withDeadline(duration: java.time.Duration) = copy(until = Some(UntilDeadline(Duration(duration.toMillis, MILLISECONDS))))

    def build(): RetryWithIncrementalBackoff = {

      require(initialDelay.isDefined, "initial delay should be defined")

      val initialDelayValue = initialDelay.get

      until match {
        case Some(UntilDeadline(duration)) =>
          require(duration > initialDelayValue, "deadline should be greater then initialDelay")

          RetryWithIncrementalBackoff(
            initialDelay = initialDelayValue,
            backoffFactor,
            maximumRetries = calculateMaxRetries(
              lastDelay = initialDelayValue,
              backoffFactor,
              deadline = duration,
              totalDelay = initialDelayValue,
              timesCounter = 1),
            maxTimeBetweenRetries,
            fireRetryExhaustedEvent)

        case Some(UntilMaximumRetries(count)) =>
          RetryWithIncrementalBackoff(
            initialDelayValue,
            backoffFactor,
            maximumRetries = count,
            maxTimeBetweenRetries,
            fireRetryExhaustedEvent)

        case None => throw new IllegalArgumentException("Either deadline of maximum retries need to be set")
      }
    }

    @tailrec
    private def calculateMaxRetries(lastDelay: Duration,
                                    backoffFactor: Double,
                                    deadline: Duration,
                                    totalDelay: Duration,
                                    timesCounter: Int): Int = {

      val newDelay = lastDelay * backoffFactor
      val nextDelay = maxTimeBetweenRetries.getOrElse(newDelay).min(newDelay) // get the minimum of two

      if ((totalDelay + nextDelay) > deadline) timesCounter
      else calculateMaxRetries(nextDelay, backoffFactor, deadline, totalDelay + nextDelay, timesCounter + 1)
    }
  }

  def fireEvent(): FireEventAfterFailure = FireEventAfterFailure(None)

  def fireEvent(eventClass: Class[_]): FireEventAfterFailure = fireEvent(eventClass.getSimpleName)

  def fireEvent(eventName: String): FireEventAfterFailure =
    FireEventAfterFailure(Some(eventName))
}
