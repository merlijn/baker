package com.ing.baker.il.petrinet

import org.scalatest._
import org.scalatest.matchers._

class MissingEventTransitionSpec extends wordspec.AnyWordSpec with should.Matchers {

  "MissingEventTransition" should {

    "correctly compute it's id" in {
      MissingEventTransition("testTransition").id should be(-3991477867259255746l)
    }
  }
}
