package com.ing.baker.recipe

package object javadsl {
  val processIdName = "$ProcessId$"

  val processId: Ingredient = Ingredient.reflect[String](processIdName)

  val exhaustedEventAppend = "RetryExhausted"
}
