package com.ing.baker.il.petrinet

import org.scalatest.*
import org.scalatest.matchers.*

class MissingEventTransitionSpec extends wordspec.AnyWordSpec with should.Matchers {

  "MissingEventTransition" should {

    "correctly compute it's id" in {
      MissingEventTransition("testTransition").id should be(-3991477867259255746l)
    }
  }
}
