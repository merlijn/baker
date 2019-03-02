package com.ing.baker.recipe

package object javadsl {
  val processIdName = "$ProcessId$"

  val processId: Ingredient = Ingredient[String](processIdName)

  val exhaustedEventAppend = "RetryExhausted"
}
