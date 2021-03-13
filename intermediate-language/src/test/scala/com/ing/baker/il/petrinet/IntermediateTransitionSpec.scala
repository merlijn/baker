package com.ing.baker.il.petrinet

import org.scalatest.*
import org.scalatest.matchers.*

class IntermediateTransitionSpec extends wordspec.AnyWordSpec with should.Matchers {

  "IntermediateTransitionSpec" should {

    "correctly compute it's id" in {
      IntermediateTransition("testTransition").id should be(-401713565417492236l)
    }
  }
}
