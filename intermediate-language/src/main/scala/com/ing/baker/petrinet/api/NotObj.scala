package com.ing.baker.petrinet.api

import scala.util.NotGiven

object NotObj {
  sealed trait SubTypeOf[A, B]

  given isSubType[A, B >: A]: SubTypeOf[A, B] = null

  type Not = [A] =>> [B] =>> NotGiven[SubTypeOf[A, B]]
}
