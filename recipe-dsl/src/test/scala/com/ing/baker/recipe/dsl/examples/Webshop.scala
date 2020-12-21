package com.ing.baker.recipe.dsl.examples

import com.ing.baker.recipe.dsl.{Event, Ingredient, Interaction, Recipe}

object Webshop {

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
          .withRequiredEvents(goodsShipped)
      )
      .withSensoryEvents(Set(
        customerInfoReceived,
        orderPlaced,
        paymentMade))
}
