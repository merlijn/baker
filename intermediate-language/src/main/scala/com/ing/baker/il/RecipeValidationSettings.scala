package com.ing.baker.il

object RecipeValidationSettings {
  val defaultValidationSettings = RecipeValidationSettings()
}

/**
  * Depending on the validations settings the following validations are done:
  *
  *   1. Check if there are any cycles
  *   2. Check if there are any disconnected graphs
  *   3. Check if there exist any non-executable interaction or not
  *
  * @param allowCycles
  * @param allowDisconnectedness
  * @param allowNonExecutableInteractions
  */
case class RecipeValidationSettings(allowCycles: Boolean = true,
                                    allowDisconnectedness: Boolean = true,
                                    allowNonExecutableInteractions: Boolean = true)
