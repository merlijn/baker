package com.ing.baker.recipe.javadsl

object Examples {

  object webshop {

    case class CustomerInfo(name: String, address: String, email: String)

    // ingredients

    val customerInfo = Ingredient[CustomerInfo]("customerInfo")
    val goods = Ingredient[String]("goods")
    val trackingId = Ingredient[String]("trackingId")
    val order = Ingredient[String]("order")
    val name = Ingredient[String]("name")
    val address = Ingredient[String]("address")
    val email = Ingredient[String]("email")

    // events

    val goodsShipped = Event("GoodsShipped",  providedIngredients = Seq(trackingId))
    val orderPlaced = Event("OrderPlaced",  providedIngredients = Seq(order))
    val customer = Event("Customer",  providedIngredients = Seq(name, address, email))
    val customerInfoReceived = Event(name = "CustomerInfoReceived",  providedIngredients = Seq(customerInfo))
    val paymentMade = Event(name = "PaymentMade",  providedIngredients = Seq.empty)
    val valid = Event(name = "Valid", providedIngredients = Seq.empty)
    val sorry = Event(name = "Sorry", providedIngredients = Seq.empty)
    val goodsManufactured = Event("GoodsManufactured",  providedIngredients = Seq(goods))
    val invoiceWasSent = Event("InvoiceWasSent", providedIngredients = Seq.empty)

    // interactions

    val validateOrder = Interaction(
      name = "ValidateOrder",
      input = Seq(order),
      output = Seq(valid, sorry))

    val manufactureGoods = Interaction(
      name = "ManufactureGoods",
      input = Seq(order),
      output = Seq(goodsManufactured))

    val sendInvoice = Interaction(
      name = "SendInvoice",
      input = Seq(customerInfo),
      output = Seq(invoiceWasSent))

    val shipGoods = Interaction(
      name = "ShipGoods",
      input = Seq(goods, customerInfo),
      output = Seq(goodsShipped)
    )

    // recipe

    val webShopRecipe: Recipe =
      Recipe("WebShop")
        .withInteractions(
          validateOrder,
          manufactureGoods
            .copy(requiredEvents = Set(valid.name, paymentMade.name)),
          shipGoods,
          sendInvoice
            .withRequiredEvent(goodsShipped)
        )
        .withSensoryEvents(Set(
          customerInfoReceived,
          orderPlaced,
          paymentMade))
  }

  object open_account {

    // ingredients

    val iban = Ingredient[String]("iban")
    val name = Ingredient[String]("name")
    val address = Ingredient[String]("address")
    val customerId = Ingredient[String]("customerId")

    val getAccountFailedReason = Ingredient[String]("getAccountFailedReason")
    val registerIndividualFailedReason = Ingredient[String]("registerIndividualFailedReason")
    val assignAccountFailedReason = Ingredient[String]("registerIndividualFailedReason")

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
        getAccount.withRequiredEvent(termsAndConditionsAccepted),
        registerIndividual)
      .withSensoryEvents(Set(
        termsAndConditionsAccepted,
        individualInformationSubmitted))
  }

  object onboarding {

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
      input =Seq(customerId),
      output = Seq(accountOpenedEvent, accountOpenedFailedEvent))

    val onboardingRecipe: Recipe =
      Recipe("newCustomerRecipe")
        .withInteractions(
          createCustomer
            .withRequiredEvent(
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
}
