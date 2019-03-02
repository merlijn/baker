package com.ing.baker.recipe.javadsl

import org.scalatest.{Matchers, WordSpecLike}


class InteractionSpec extends WordSpecLike with Matchers {
  "an Interaction" when {
    "calling the Equals method" should {
      "return true if same interaction instance" in {
        val customerName = Ingredient[String]("customerName")
        val createCustomer = Interaction(
          name = "CreateCustomer",
          input = Seq(customerName),
          output = Seq()
        )
        createCustomer.equals(createCustomer) shouldBe true
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
        CreateCustomer.equals(CreateCustomer2) shouldBe true
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
        CreateCustomer.equals(CreateCustomer2) shouldBe false
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
        CreateCustomer.equals(otherObject) shouldBe false
      }
    }
  }
}
