package com.ing.baker.recipe.dsl.examples

import com.ing.baker.recipe.dsl.{Event, Ingredient, Interaction, Recipe}

object Onboarding {

  //Ingredients
  val customerName = Ingredient[String]("customerName")
  val customerId = Ingredient[String]("customerId")
  val accountId = Ingredient[Integer]("accountId")
  val accountName = Ingredient[Integer]("accountName")

  //Events
  val agreementsAcceptedEvent = Event(name = "agreementsAccepted", providedIngredients = Seq.empty)
  val manualApprovedEvent = Event(name = "manualApproved", providedIngredients = Seq.empty)
  val automaticApprovedEvent = Event(name = "automaticApproved", providedIngredients = Seq.empty)
  val NameProvidedEvent = Event(name = "nameProvided", providedIngredients = Seq(customerName))
  val accountOpenedEvent = Event(name = "accountOpened", providedIngredients = Seq(accountId, accountName))
  val accountOpenedFailedEvent = Event(name = "accountOpenedFailed", providedIngredients = Seq.empty)
  val createCustomerSuccessful = Event(name = "CreateCustomerSuccessful", providedIngredients = Seq(customerId))

  //Recipe
  //Interactions
  val createCustomer = Interaction(
    name = "CreateCustomer",
    input = Seq(customerName),
    output = Seq(createCustomerSuccessful)
  )

  val openAccount = Interaction(
    name = "OpenAccount",
    input = Seq(customerId),
    output = Seq(accountOpenedEvent, accountOpenedFailedEvent))

  val onboardingRecipe: Recipe =
    Recipe("newCustomerRecipe")
      .withInteractions(
        createCustomer
          .withRequiredEvents(
            agreementsAcceptedEvent)
          .withRequiredOneOfEvents(Set(
            automaticApprovedEvent,
            manualApprovedEvent)),
        openAccount
          .withEventOutputTransformer(
            accountOpenedEvent,
            "newAccountOpenedEvent",
            Map.empty)
      )
      .withSensoryEvents(Set(
        agreementsAcceptedEvent,
        NameProvidedEvent,
        manualApprovedEvent,
        automaticApprovedEvent
      ))
}
