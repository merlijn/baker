package com.ing.baker.il.recipe

import com.ing.baker.il.InteractionFailureStrategy.RetryWithIncrementalBackoff
import com.ing.baker.il.InteractionFailureStrategyOutcome.{BlockTransition, RetryWithDelay}
import org.scalatest.*
import org.scalatest.matchers.*

import scala.concurrent.duration.*

class InteractionFailureStrategySpec extends wordspec.AnyWordSpec with should.Matchers {

  "The RetryWithIncrementalBackoff" should {
    "return RetryWithDelay with the correct time until the next retry" in {
      val retry = RetryWithIncrementalBackoff(Duration(1, MILLISECONDS), 2.0, 5, None, None)
      retry(1) should be (RetryWithDelay(1))
      retry(2) should be (RetryWithDelay(2))
      retry(3) should be (RetryWithDelay(4))
      retry(4) should be (RetryWithDelay(8))
      retry(5) should be (RetryWithDelay(16))
    }

    "return BlockTransition when retry max times fired is met" in {
      val retry = RetryWithIncrementalBackoff(Duration(1, MILLISECONDS), 2.0, 5, None, None)
      retry(6) should be (BlockTransition)
    }

    "return the max retry time when retry max time is met" in {
      val retry = RetryWithIncrementalBackoff(Duration(1, MILLISECONDS), 2.0, 10, Some(Duration(10, MILLISECONDS)), None)
      retry(4) should be (RetryWithDelay(8))
      retry(5) should be (RetryWithDelay(10))
      retry(6) should be (RetryWithDelay(10))
    }

    "Not fail if retry gets to big" in {
      val retry = RetryWithIncrementalBackoff(Duration(10, MILLISECONDS), 2.0, 100, Some(Duration(10, MILLISECONDS)), None)
      retry(42) should be (RetryWithDelay(10))
      retry(101) should be (BlockTransition)
    }
  }
}
