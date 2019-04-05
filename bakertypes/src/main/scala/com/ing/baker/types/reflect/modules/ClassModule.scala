package com.ing.baker.types.reflect.modules

import java.lang.reflect

import com.ing.baker.types.reflect.Reflect._
import com.ing.baker.types.reflect.TypeModule

import scala.reflect.runtime.universe.TypeTag

abstract class ClassModule[T : TypeTag] extends TypeModule {

  protected val clazz = mirror.runtimeClass(mirror.typeOf[T])

  override def isApplicable(javaType: reflect.Type): Boolean = isAssignableToBaseClass(javaType, clazz)
}
