package com.ing.baker.runtime.core.internal

import com.ing.baker.il.recipe.petrinet.InteractionTransition
import java.util.concurrent.ConcurrentHashMap
import com.ing.baker.runtime.core.InteractionImplementation

import scala.compat.java8.FunctionConverters.*

/**
  * The InteractionManager is responsible for all implementation of interactions.
  * It knows all available implementations and gives the correct implementation for an Interaction
  *
  * @param interactionImplementations All
  */
class InteractionManager {
  

  /**
    * Gets an implementation is available for the given interaction.
    * It checks:
    *   1. Name
    *   2. Input variable sizes
    *   3. Input variable types
    *
    * @param interaction The interaction to check
    * @return An option containing the implementation if available
    */
  def getImplementation(interaction: InteractionTransition): Option[InteractionImplementation] = ???
}
