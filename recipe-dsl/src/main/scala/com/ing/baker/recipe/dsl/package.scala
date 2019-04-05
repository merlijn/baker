package com.ing.baker.recipe

package object dsl {
  val processIdName = "$ProcessId$"

  val processId: Ingredient = Ingredient.reflect[String](processIdName)

  val exhaustedEventAppend = "RetryExhausted"
}
