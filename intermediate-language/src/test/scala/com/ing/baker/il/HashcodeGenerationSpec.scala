package com.ing.baker.il

import org.scalatest._
import org.scalatest.matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalacheck.{Gen, Prop, Test}

class HashcodeGenerationSpec extends wordspec.AnyWordSpec with should.Matchers with ScalaCheckDrivenPropertyChecks {
  
  def hash(str: String): Long = sha256HashCode(str)

  "The sha256 hash function" should {
    "not give hash collisions" in {
      
      forAll(Gen.alphaNumStr, Gen.alphaNumStr)  {
        (s1: String, s2: String) => {
          if (s1 != s2) 
            hash(s1) should not be(hash(s2))
          else 
            hash(s1) should be (hash(s2))
        }
      }
    }
  }
}
