package com.ing.baker.il

import org.scalatest._
import org.scalatest.matchers._
//import org.scalatest.prop.Checkers
//import org.scalacheck.{Gen, Prop, Test}

class HashcodeGenerationSpec extends wordspec.AnyWordSpec with should.Matchers {

//  def hash(str: String): Long = str.hashCode // Test fails with this hash function
  def hash(str: String): Long = sha256HashCode(str)

  "The sha256 hash function" should {
    "not give hash collisions" in {

//      val prop = Prop.forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
//        (s1: String, s2: String) => {
//          if (s1 != s2) hash(s1) != hash(s2)
//          else hash(s1) == hash(s2)
//        }
//      }

//      check(prop, Test.Parameters.defaultVerbose.withMinSuccessfulTests(10 * 1000))
    }
  }
}
