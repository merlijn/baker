package com.ing.baker.runtime.core

/**
  * Provides an implementation for an interaction.
  */
trait InteractionImplementation {

  /**
    * The name of the interaction
    */
  val name: String

  /**
    * Executes the interaction.
    *
    * TODO return type should be async
    * TODO input could be map instead of sequence??
    *
    * @param input
    * @return
    */
  def execute(input: Seq[(String, Any)]): Option[ProcessEvent]
}
