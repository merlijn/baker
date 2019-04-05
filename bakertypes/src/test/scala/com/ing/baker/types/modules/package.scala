package com.ing.baker.types

import com.ing.baker.types.reflect.Reflect
import org.scalacheck.Prop.{BooleanOperators, forAll}
import org.scalacheck.{Gen, Prop}

import scala.reflect.runtime.universe.TypeTag

package object modules {

  def transitivityProperty[T : TypeTag](gen: Gen[T]): Prop = {

    val parsedType = Reflect.readJavaType[T]

    forAll(gen) { original =>

      val value = Reflect.toValue(original)
      val parsed = Reflect.toJava[T](value)

      value.isInstanceOf(parsedType) :| s"$value is not an instance of $parsedType" &&
        parsed.equals(original) :| s"$value != $parsed"
    }
  }
}
