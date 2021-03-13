package com.ing.baker.recipe.dsl

import org.scalatest.*
import org.scalatest.matchers.*

import InteractionDescriptorSpec.*

object InteractionDescriptorSpec {
  val customerName = Ingredient[String]("customerName")
  val customerId = Ingredient[String]("customerId")
  val createCustomer = Interaction(
    name = "CreateCustomer",
    input = Seq(customerName),
    output = Seq()
  )
  val agreementsAcceptedEvent = Event("agreementsAccepted",  providedIngredients = Seq.empty)
  val anOtherEvent = Event("anOtherEvent",  providedIngredients = Seq.empty)
}

class InteractionDescriptorSpec extends wordspec.AnyWordSpec with should.Matchers {
  "an InteractionDescriptor" when {

    "requiredEvents called" should {
      "update the requiredEventsList" in {
        val updated = createCustomer.withRequiredEvents(agreementsAcceptedEvent)
        updated.requiredEvents should be(Set(agreementsAcceptedEvent.name))
      }
    }

    "requiredOneOfEvents called" should {
      "updates the requiredOneOfEventsList" in {
        val updated = createCustomer.withRequiredOneOfEvents(Set(agreementsAcceptedEvent, anOtherEvent))
        updated.requiredOneOfEvents.head should be (Set(agreementsAcceptedEvent.name, anOtherEvent.name))
      }

      "throws IllegalArgumentException if nr of events is less than 2" in {
        assertThrows[IllegalArgumentException] {
          createCustomer.withRequiredOneOfEvents(Set(agreementsAcceptedEvent))
        }
      }
    }
  }
}
