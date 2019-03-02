package com.ing.baker.recipe.json

import org.scalatest.{Matchers, WordSpecLike}

class ParseJsonSpec extends WordSpecLike with Matchers {

  val jsonString =
    """
      |{
      |   "name" : "webshop",
      |   "sensoryEvents" : [
      |      {
      |        "name" : "com.example.webhop.OrderPlaced",
      |        "providedIngredients" : [
      |            {
      |              "name"   : "order",
      |              "schema" : { "type" : "string" },
      |            }
      |         ]
      |      }
      |      {
      |         "name" : "com.example.webhop.CustomerInfoReceived",
      |         "providedIngredients" : [
      |           {
      |             "name" : "customerInfo",
      |             "schema" : {
      |               "type" : "object",
      |               "properties" : {
      |                 "name" : { "type" : "string" },
      |                 "address" : { "type" : "string" },
      |                 "email" : { "type" : "string" }
      |               }
      |             }
      |           }
      |         ]
      |      }
      |   ],
      |   "interactions" : [
      |     {
      |       "name" : "ValidateOrder",
      |       "input" : [
      |         {
      |           "name" : "order",
      |           "schema" : { "type" : "string" }
      |         }
      |       ],
      |       "output" : [
      |         {
      |            "name" : "Valid",
      |            "providedIngredients" : [ ]
      |         },
      |         {
      |            "name" : "Sorry",
      |            "providedIngredients" : [ ]
      |         }
      |       ]
      |     }
      |   ]
      |}
    """.stripMargin


  "the json dsl" should {

    "parse a simple example" in {

      import net.liftweb.json._

      implicit val formats = DefaultFormats

      val json = parse(jsonString)

      val recipe = json.extract[Recipe]

      println(prettyRender(json))
    }
  }

}
