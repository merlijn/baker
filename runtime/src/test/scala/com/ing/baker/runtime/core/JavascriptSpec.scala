package com.ing.baker.runtime.core

import com.ing.baker.compiler.RecipeCompiler
import com.ing.baker.recipe.dsl.examples.Webshop
import org.scalatest.{Matchers, WordSpec}

class JavascriptSpec extends WordSpec with Matchers {

  "The javascript engine" should {

    "do something" in {

      val script  =
        """
          |
          | InvoiceWasSent ( { hello : "world" } );
          |
        """.stripMargin

      val recipe = RecipeCompiler.compileRecipe(Webshop.webShopRecipe)

      val i = recipe.interactionTransitions.find(_.originalName == "SendInvoice").get

      val impl = new JavaScriptInteractionImpl(i, script)

      impl.execute(Seq.empty)
    }
  }

}
