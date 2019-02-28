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
      |            { "name" : "orderId" },
      |            { "name" : "date" },
      |         ]
      |      }
      |   ],
      |   "interactions" : [
      |
      |   ]
      |}
    """.stripMargin


  "the json dsl" should {

    "parse a simple example" in {

      import net.liftweb.json._

      implicit val formats = DefaultFormats

      val json = parse(jsonString)

      val recipe = json.extract[Recipe]

      println(recipe)
    }
  }

}
