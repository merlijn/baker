package com.ing.baker.runtime.core

/**
  * A status object indicating how baker reacted to an event it received.
  */
enum SensoryEventStatus:

    /**
      * The event was successfully received.
      */
    case OK
    /**
      * The firing limit, the number of times this event may fire, was met.
      */
    case FiringLimitReached
    /**
      * The receive period in which events may be accepted was expired for this process instance.
      */
    case ReceivePeriodExpired
    /**
      * An event with the same correlation id was already received.
      */
    case AlreadyReceived
    /**
      * The process instance was deleted.
      */
    case ProcessDeleted