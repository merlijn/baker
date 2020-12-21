package com.ing.baker.recipe.dsl

object Constants {
  
  val processIdName = "$ProcessId$"

  val processId: Ingredient[String] = Ingredient[String](processIdName)

  val exhaustedEventAppend = "RetryExhausted"
}
