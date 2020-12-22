package com.ing.baker.recipe.dsl

import org.scalatest._
import org.scalatest.matchers._

class IngredientSpec extends wordspec.AnyWordSpec with should.Matchers {
  "an Ingredient" when {

    "calling the Equals method" should {

      "return true if same ingredient instance" in {
        val customerName = Ingredient[String]("customerName")
        customerName.equals(customerName) should be (true)
      }

      "return true if different ingredient with same name and type" in {
        val customerName = Ingredient[String]("customerName")
        val customerName2 = Ingredient[String]("customerName")
        customerName.equals(customerName2) should be (true)
      }

      "return false if different ingredient with different name and same type" in {
        val customerName = Ingredient[String]("customerName")
        val agreementName = Ingredient[String]("agreementName")
        customerName.equals(agreementName) should be (false)
      }

// TODO This can be checked at compile time in the dsl 
//      "return false if different ingredient with same name and different type" in {
//        val customerName = Ingredient[String]("customerName")
//        val customerName2 = Ingredient[Integer]("customerName")
//        customerName.equals(customerName2) should be (false)
//      }

      "return false if different ingredient with different name and different type" in {
        val customerName = Ingredient[String]("customerName")
        val agreementName = Ingredient[Integer]("agreementName")
        customerName.equals(agreementName) should be (false)
      }

      "return false if different object" in {
        val customerName = Ingredient[String]("customerName")
        val otherObject = ""
        customerName.equals(otherObject) should be (false)
      }
    }
  }
}