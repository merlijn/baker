package com.ing.baker.runtime.core.internal

/**
  * Exception thrown by an interaction that cannot be recovered by retrying.
  */
class FatalInteractionException(message: String, cause: Throwable = null) extends RuntimeException(message, cause)
