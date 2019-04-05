package com.ing.baker.recipe.dsl

import com.ing.baker.types.reflect.Reflect
import com.ing.baker.types.{Type, Value}

import scala.reflect.runtime.{universe => ru}

object Ingredient {

  def reflect[T : ru.TypeTag](name: String): Ingredient = Ingredient(name, Reflect.readJavaType[T])
}

case class Ingredient(val name: String, val ingredientType: Type) {

  def apply(value: Any): (String, Value) = name -> Reflect.toValue(value)
}
