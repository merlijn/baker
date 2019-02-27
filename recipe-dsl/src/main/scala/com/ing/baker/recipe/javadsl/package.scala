package com.ing.baker.recipe

import java.lang.reflect.Method

import com.ing.baker.recipe.javadsl.ReflectionHelpers._
import com.ing.baker.types.{Converters, Type}
import org.reflections.Reflections

package object javadsl {

  private val interactionMethodName: String = "apply"

  def createIngredient(ingredientName: String, ingredientType: Type): common.Ingredient =
    new common.Ingredient(
      name = ingredientName,
      ingredientType = ingredientType
    )

  def parseType(javaType: java.lang.reflect.Type, errorMessage: String): Type = {
    try {
      Converters.readJavaType(javaType)
    } catch {
      case e: Exception => throw new IllegalArgumentException(errorMessage, e)
    }
  }

  def eventClassToCommonEvent(eventClass: Class[_], firingLimit: Option[Int]): common.Event = new javadsl.Event(eventClass, firingLimit)

  def interactionClassToCommonInteraction(interactionClass: Class[_], newName: Option[String]): InteractionDescriptor = {

    val name: String = interactionClass.getSimpleName

    val method: Method = interactionClass.getDeclaredMethods
      .find(_.getName == interactionMethodName)
      .getOrElse(throw new IllegalStateException(
        s"No method named '$interactionMethodName' defined on '${interactionClass.getName}'"))

    val inputIngredients: Seq[common.Ingredient] =
      method.getParameterNames.map(name =>
        createIngredient(name,
          parseType(
            method.parameterTypeForName(name).get,
            s"Unsupported type for ingredient '$name' on interaction '${interactionClass.getName}'")))

    def getOutputClasses(): Seq[Class[_]] = {
      import scala.collection.JavaConverters._

      if (method.isAnnotationPresent(classOf[annotations.FiresEvent])) {

        val outputEventClasses: Seq[Class[_]] = method.getAnnotation(classOf[annotations.FiresEvent]).oneOf()

        if (outputEventClasses.isEmpty) {

          val returnType = method.getReturnType

          if (classOf[Unit].equals(returnType) || classOf[java.lang.Void].equals(returnType))
            Seq.empty

          // in case the return type is an interface we find all implementations in the same package
          else if (returnType.isInterface) {

            val packageName = returnType.getPackage.getName

            val reflections = new Reflections(packageName)

            val classes = reflections.getSubTypesOf(returnType).asScala.toSeq

            classes
          }
          // otherwise there is only a single return event
          else {
            Seq(returnType)
          }
        }
        else {
          outputEventClasses.foreach {
            eventClass =>
              if (!method.getReturnType.isAssignableFrom(eventClass))
                throw new common.RecipeValidationException(s"Interaction $name provides event '${eventClass.getName}' that is incompatible with it's return type")
          }

          outputEventClasses
        }
      }
      else Seq.empty
    }

    val output: Seq[common.Event] = getOutputClasses().map(eventClassToCommonEvent(_, None))

    InteractionDescriptor(name, inputIngredients, output, Set.empty, Set.empty, Map.empty, Map.empty, None, None, Map.empty, newName)
  }
}
