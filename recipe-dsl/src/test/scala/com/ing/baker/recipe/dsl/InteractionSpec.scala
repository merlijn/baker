package com.ing.baker.recipe.dsl

import org.scalatest._
import org.scalatest.matchers._


class InteractionSpec extends wordspec.AnyWordSpec with should.Matchers {
  "an Interaction" when {
    "calling the Equals method" should {
      "return true if same interaction instance" in {
        val customerName = Ingredient[String]("customerName")
        val createCustomer = Interaction(
          name = "CreateCustomer",
          input = Seq(customerName),
          output = Seq()
        )
        createCustomer.equals(createCustomer) should be(true)
      }

      "return true if different interaction instance with same signature" in {
        val customerName = Ingredient[String]("customerName")
        val customerId = Ingredient[String]("customerId")
        val CreateCustomer = Interaction(
          name = "CreateCustomer",
          input = Seq(customerName),
          output = Seq()
        )
        val CreateCustomer2 = Interaction(
          name = "CreateCustomer",
          input = Seq(customerName),
          output = Seq()
        )
        CreateCustomer.equals(CreateCustomer2) should be(true)
      }

      "return false if different interaction instance with different name" in {
        val customerName = Ingredient[String]("customerName")
        val customerId = Ingredient[String]("customerId")
        val CreateCustomer = Interaction(
          name = "CreateCustomer",
          input = Seq(customerName),
          output = Seq()
        )
        val CreateCustomer2 = Interaction(
          name = "CreateCustomer2",
          input = Seq(customerName),
          output = Seq()
        )
        CreateCustomer.equals(CreateCustomer2) should be(false)
      }

      "return false if different object" in {
        val customerName = Ingredient[String]("customerName")
        val customerId = Ingredient[String]("customerId")
        val CreateCustomer = Interaction(
          name = "CreateCustomer",
          input = Seq(customerName),
          output = Seq()
        )
        val otherObject = ""
        CreateCustomer.equals(otherObject) should be(false)
      }
    }
  }
}
