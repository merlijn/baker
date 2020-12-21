package com.ing.baker.il.petrinet

import org.scalatest._
import org.scalatest.matchers._

class IntermediateTransitionSpec extends wordspec.AnyWordSpec with should.Matchers {

  "IntermediateTransitionSpec" should {

    "correctly compute it's id" in {
      IntermediateTransition("testTransition").id should be(-401713565417492236l)
    }
  }
}
