package com.ing.baker.runtime.core

import com.ing.baker.il.InteractionFailureStrategyOutcome
import com.ing.baker.types.Value

import scala.collection.JavaConverters._

/**
  * Holds the 'state' of a process instance.
  *
  * @param processId The process identifier
  * @param ingredients The accumulated ingredients
  * @param eventNames The names of the events occurred so far
  */
case class ProcessState(processId: String,
                        ingredients: Map[String, Value],
                        eventNames: List[String]) extends Serializable