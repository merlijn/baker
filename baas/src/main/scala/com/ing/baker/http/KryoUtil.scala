package com.ing.baker.http

import com.twitter.chill.{KryoPool, ScalaKryoInstantiator}

object KryoUtil {
  val defaultKryoPool: KryoPool = KryoPool.withByteArrayOutputStream(1,
    new ScalaKryoInstantiator()
  )

  def deserialize[T](bytes: Array[Byte]): T = defaultKryoPool.fromBytes(bytes).asInstanceOf[T]

  def serialize[T](obj: T): Array[Byte] = defaultKryoPool.toBytesWithClass(obj)
}
