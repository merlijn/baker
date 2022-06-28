package com.ing.baker.runtime.core

import com.ing.baker.il.recipe.InteractionFailureStrategyOutcome

/**
  * Holds the 'state' of a process instance.
  *
  * @param processId The process identifier
  * @param ingredients The accumulated ingredients
  * @param eventNames The names of the events occurred so far
  */
case class ProcessState(processId: String,
                        ingredients: Map[String, Any],
                        eventNames: List[String]) extends Serializable