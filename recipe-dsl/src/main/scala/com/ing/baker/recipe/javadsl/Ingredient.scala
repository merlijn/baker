package com.ing.baker.recipe.javadsl

import com.ing.baker.types.{Converters, Type, Value}

import scala.language.experimental.macros
import scala.reflect.runtime.{universe => ru}

object Ingredient {
  val mirror: ru.Mirror = ru.runtimeMirror(classOf[Ingredient].getClassLoader)

  def apply[T: ru.TypeTag]: Ingredient = macro CommonMacros.ingredientImpl[T]

  def apply[T : ru.TypeTag](name: String): Ingredient = Ingredient(name, Converters.readJavaType[T])
}

case class Ingredient(val name: String, val ingredientType: Type) {

  def apply(value: Any): (String, Value) = name -> Converters.toValue(value)
}
