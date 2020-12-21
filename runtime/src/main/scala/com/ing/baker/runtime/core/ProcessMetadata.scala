package com.ing.baker.runtime.core

/**
  * This class holds some meta data of a baker process.
  *
  * @param recipeId The recipe id of the process
  * @param processId The identifier of the process
  * @param createdTime The time the process was created
  */
case class ProcessMetadata(recipeId: String, processId: String, createdTime: Long)
