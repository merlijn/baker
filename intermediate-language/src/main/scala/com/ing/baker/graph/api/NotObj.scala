package com.ing.baker.graph.api

import scala.util.NotGiven

sealed trait SubTypeOf[A, B]

given isSubType[A, B >: A]: SubTypeOf[A, B] = null

type Not = [A] =>> [B] =>> NotGiven[SubTypeOf[A, B]]