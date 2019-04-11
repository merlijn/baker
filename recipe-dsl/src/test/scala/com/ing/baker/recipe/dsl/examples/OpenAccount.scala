package com.ing.baker.recipe.dsl.examples

import com.ing.baker.recipe.dsl._

object OpenAccount {

  // ingredients

  val iban = Ingredient.reflect[String]("iban")
  val name = Ingredient.reflect[String]("name")
  val address = Ingredient.reflect[String]("address")
  val customerId = Ingredient.reflect[String]("customerId")

  val getAccountFailedReason = Ingredient.reflect[String]("getAccountFailedReason")
  val registerIndividualFailedReason = Ingredient.reflect[String]("registerIndividualFailedReason")
  val assignAccountFailedReason = Ingredient.reflect[String]("registerIndividualFailedReason")

  // events

  val getAccountSuccessful = Event(name = "GetAccountSuccessful", providedIngredients = Seq(iban))
  val getAccountFailed = Event(name = "GetAccountFailed", providedIngredients = Seq(getAccountFailedReason))

  val assignAccountSuccessful = Event(name = "AssignAccountSuccessful", providedIngredients = Seq.empty)
  val assignAccountFailed = Event(name = "AssignAccountFailed",  providedIngredients = Seq(assignAccountFailedReason))

  val registerIndividualSuccessful = Event(name = "RegisterIndividualSuccessful",  providedIngredients = Seq(customerId))
  val registerIndividualFailed = Event(name = "RegisterIndividualFailed",  providedIngredients = Seq(registerIndividualFailedReason))

  val termsAndConditionsAccepted = Event(name = "TermsAndConditionsAccepted", providedIngredients = Seq.empty)
  val individualInformationSubmitted = Event(name = "individualInformationSubmitted",  providedIngredients = Seq(name, address))

  // interactions

  val getAccount = Interaction(
    name = "GetAccount",
    input = Seq.empty,
    output = Seq(getAccountSuccessful, getAccountFailed)
  )

  val assignAccount = Interaction(
    name = "AssignAccount",
    input = Seq(customerId, iban),
    output = Seq(assignAccountSuccessful, assignAccountFailed)
  )

  val registerIndividual = Interaction(
    name = "RegisterIndividual",
    input = Seq(name, address),
    output = Seq(registerIndividualSuccessful, registerIndividualFailed)
  )

  // recipe

  val openAccountRecipe = Recipe("OpenAccountRecipe")
    .withInteractions(
      assignAccount,
      getAccount.withRequiredEvents(termsAndConditionsAccepted),
      registerIndividual)
    .withSensoryEvents(Set(
      termsAndConditionsAccepted,
      individualInformationSubmitted))
}
