package com.ing.baker.runtime

import java.util.UUID

import akka.testkit.{TestDuration, TestKit}
import com.ing.baker.BakerRuntimeTestBase
import com.ing.baker.compiler.RecipeCompiler
import com.ing.baker.runtime.ScalaDSLRuntime._
import com.ing.baker.runtime.core.Baker

import scala.concurrent.duration._

class ExamplesSpec extends BakerRuntimeTestBase  {
  override def actorSystemName = "ExamplesSpec"

  "The WebShop recipe" should {

    import com.ing.baker.recipe.javadsl.Examples.webshop._

    "compile without validation errors" in {

      // compiles the recipe
      val compiledRecipe = RecipeCompiler.compileRecipe(webShopRecipe)

//      println(s"Visual recipe: ${compiledRecipe.getRecipeVisualization}")

      // prints any validation errors the compiler found
      compiledRecipe.validationErrors shouldBe empty
    }

    "run a happy flow" in {

      // compiles the recipe
      val compiledRecipe = RecipeCompiler.compileRecipe(webShopRecipe)

      // test data
      val testCustomerInfoData = CustomerInfo("John Snow", "Winterfell", "john_snow@hotmail.com")
      val testOrder = "Valyrian steel sword"
      val testGoods = "Valyrian steel sword instance"
      val testTrackingId = "001"

      // create implementations of the interactions
      val validateOrderImpl = validateOrder implement {
        (order: String) => {
          // Some logic here
          valid.instance() // or maybe invalid event to be returned
        }
      }

      val manufactureGoodsImpl = manufactureGoods implement {
        (order: String) => {
          // Some logic here
          goodsManufactured.instance(testGoods)
        }
      }

      val sendInvoiceImpl = sendInvoice implement {
        (customerInfo: CustomerInfo) => invoiceWasSent.instance()
      }

      val shipGoodsImpl = shipGoods implement {
        (goods: String, customerInfo: CustomerInfo) => goodsShipped.instance(testTrackingId)
      }

      val implementations =
        Seq(validateOrderImpl, manufactureGoodsImpl, sendInvoiceImpl, shipGoodsImpl)

      val baker = new Baker()

      implementations.foreach(baker.addImplementation)

      val recipeId = baker.addRecipe(compiledRecipe)

      val processId = UUID.randomUUID().toString

      baker.createProcess(recipeId, processId)

      implicit val timeout: FiniteDuration = 2.seconds.dilated

      // fire events

      baker.fireEvent(processId, orderPlaced.instance(testOrder))
      baker.fireEvent(processId, paymentMade.instance())
      baker.fireEvent(processId, customerInfoReceived.instance(testCustomerInfoData))

      val expectedIngredients = IngredientMap(
        order -> testOrder,
        goods -> testGoods,
        customerInfo -> testCustomerInfoData,
        trackingId -> testTrackingId)

      val actualIngredients = baker.getIngredients(processId)

      // assert the that all ingredients are provided
      actualIngredients shouldBe expectedIngredients

      val expectedEvents = List(
        orderPlaced.instance(testOrder),
        valid.instance(),
        paymentMade.instance(),
        goodsManufactured.instance(testGoods),
        customerInfoReceived.instance(testCustomerInfoData),
        goodsShipped.instance(testTrackingId),
        invoiceWasSent.instance())

      TestKit.awaitCond(baker.getEvents(processId) equals expectedEvents, 2.seconds.dilated)
    }
  }

  "The open account recipe" should {

    import com.ing.baker.recipe.javadsl.Examples.open_account._

    "compile without validation errors" in {

      // compiles the recipe
      val compiledRecipe = RecipeCompiler.compileRecipe(openAccountRecipe)

      // prints any validation errors the compiler found
      compiledRecipe.validationErrors shouldBe empty
    }
  }
}
