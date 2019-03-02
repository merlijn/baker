package com.ing.baker.recipe.javadsl

import org.scalatest.{Matchers, WordSpecLike}

import InteractionDescriptorSpec._

object InteractionDescriptorSpec {
  val customerName = Ingredient[String]("customerName")
  val customerId = Ingredient[String]("customerId")
  val createCustomer = Interaction(
    name = "CreateCustomer",
    input = Seq(customerName),
    output = Seq()
  )
  val agreementsAcceptedEvent = Event("agreementsAccepted")
  val anOtherEvent = Event("anOtherEvent")
}

class InteractionDescriptorSpec extends WordSpecLike with Matchers {
  "an InteractionDescriptor" when {

    "requiredEvents called" should {
      "update the requiredEventsList" in {
        val updated = createCustomer.withRequiredEvent(agreementsAcceptedEvent)
        updated.requiredEvents shouldBe Set(agreementsAcceptedEvent.name)
      }
    }

    "requiredOneOfEvents called" should {
      "updates the requiredOneOfEventsList" in {
        val updated = createCustomer.withRequiredOneOfEvents(Set(agreementsAcceptedEvent, anOtherEvent))
        updated.requiredOneOfEvents.head shouldBe Set(agreementsAcceptedEvent.name, anOtherEvent.name)
      }

      "throws IllegalArgumentException if nr of events is less than 2" in {
        intercept[IllegalArgumentException] {
          createCustomer.withRequiredOneOfEvents(Set(agreementsAcceptedEvent))
        }
      }
    }
  }
}
