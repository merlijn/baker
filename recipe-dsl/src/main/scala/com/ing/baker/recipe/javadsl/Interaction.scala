package com.ing.baker.recipe.javadsl

import java.lang.reflect.Method

import com.ing.baker.recipe.javadsl.ReflectionHelpers._
import com.ing.baker.recipe.{annotations, javadsl}
import com.ing.baker.types.Converters
import org.reflections.Reflections

import scala.annotation.varargs
import scala.collection.JavaConverters._

case class Interaction private(
      name: String,
      input: Seq[Ingredient],
      output: Seq[Event],
      originalName: Option[String] = None,
      requiredEvents: Set[String] = Set.empty,
      requiredOneOfEvents: Set[Set[String]] = Set.empty,
      predefinedIngredients: Map[String, com.ing.baker.types.Value] = Map.empty,
      renamedInputIngredients: Map[String, String] = Map.empty,
      maximumExecutionCount: Option[Int] = None,
      failureStrategy: Option[InteractionFailureStrategy] = None,
      eventOutputTransformers: Map[Event, EventOutputTransformer] = Map.empty) {

  /**
    * The retry exhausted event name
    *
    * @return
    */
  def retryExhaustedEventName: String = name + exhaustedEventAppend

  /**
    * This sets a requirement for this interaction that a specific event needs to have been fired before it can execute.
    *
    * @param newRequiredEvent the class of the events that needs to have been fired
    * @return
    */
  def withRequiredEvent(newRequiredEvent: Class[_]): Interaction =
    copy(requiredEvents = requiredEvents + newRequiredEvent.getSimpleName)

  /**
    * This sets a requirement for this interaction that some specific events needs to have been fired before it can execute.
    *
    * @param newRequiredEvents the classes of the events.
    * @return
    */
  @SafeVarargs
  @varargs
  def withRequiredEvents(newRequiredEvents: Class[_]*): Interaction =
    copy(requiredEvents = requiredEvents ++ newRequiredEvents.map(_.getSimpleName))

  /**
    * This sets a requirement for this interaction that some specific events needs to have been fired before it can execute.
    *
    * @param newRequiredEvents the classes of the event.
    * @return
    */
  def withRequiredEvents(newRequiredEvents: java.util.Set[Class[_]]): Interaction =
    copy(requiredEvents = requiredEvents ++ newRequiredEvents.asScala.map(_.getSimpleName))


  /**
    * This sets a requirement for this interaction that a specific event needs to have been fired before it can execute.
    *
    * @param newRequiredEventName the name of the events that needs to have been fired
    * @return
    */
  def withRequiredEventFromName(newRequiredEventName: String): Interaction =
    copy(requiredEvents = requiredEvents + newRequiredEventName)

  /**
    * This sets a requirement for this interaction that some specific events needs to have been fired before it can execute.
    *
    * @param newRequiredEventNames the names of the events.
    * @return
    */
  @SafeVarargs
  @varargs
  def withRequiredEventsFromName(newRequiredEventNames: String*): Interaction =
    copy(requiredEvents = requiredEvents ++ newRequiredEventNames)

  /**
    * This sets a requirement for this interaction that some specific events needs to have been fired before it can execute.
    *
    * @param newRequiredEvents the names of the events.
    * @return
    */
  def withRequiredEventsFromName(newRequiredEvents: java.util.Set[String]): Interaction =
    copy(requiredEvents = requiredEvents ++ newRequiredEvents.asScala)

  /**
    * This sets a requirement for this interaction that one of the given events needs to have been fired before it can execute.
    *
    * @param newRequiredOneOfEvents the classes of the events.
    * @return
    */
  @SafeVarargs
  @varargs
  def withRequiredOneOfEvents(newRequiredOneOfEvents: Class[_]*): Interaction = {
    if (newRequiredOneOfEvents.nonEmpty && newRequiredOneOfEvents.size < 2)
      throw new IllegalArgumentException("At least 2 events should be provided as 'requiredOneOfEvents'")

    val newRequired: Set[Set[String]] = requiredOneOfEvents + newRequiredOneOfEvents.map(_.getSimpleName).toSet

    copy(requiredOneOfEvents = newRequired)
  }

  /**
    * This sets a requirement for this interaction that one of the given events needs to have been fired before it can execute.
    *
    * @param newRequiredOneOfEvents the names of the events.
    * @return
    */
  @SafeVarargs
  @varargs
  def withRequiredOneOfEventsFromName(newRequiredOneOfEvents: String*): Interaction = {
    if (newRequiredOneOfEvents.nonEmpty && newRequiredOneOfEvents.size < 2)
      throw new IllegalArgumentException("At least 2 events should be provided as 'requiredOneOfEvents'")
    val newRequired: Set[Set[String]] = requiredOneOfEvents + newRequiredOneOfEvents.toSet

    copy(requiredOneOfEvents = newRequired)
  }

  /**
    * This sets a input ingredient to a set value. In this case the ingredient wont be taken from the runtime recipe.
    *
    * @param ingredientName  the name of the ingredient
    * @param ingredientValue the value of the ingredient
    * @return
    */
  def withPredefinedIngredient(ingredientName: String,
                               ingredientValue: AnyRef): Interaction =
    addPredefinedIngredient(Map(ingredientName -> ingredientValue))

  /**
    * This sets two input ingredient to a set value. In this case the ingredients wont be taken from the runtime recipe.
    *
    * @param ingredientName1  the name of the first ingredient
    * @param ingredientValue1 the value of first the ingredient
    * @param ingredientName2  the name of the second ingredient
    * @param ingredientValue2 the value of second the ingredient
    * @return
    */
  def withPredefinedIngredients(ingredientName1: String,
                                ingredientValue1: AnyRef,
                                ingredientName2: String,
                                ingredientValue2: AnyRef): Interaction =
    addPredefinedIngredient(
      Map(ingredientName1 -> ingredientValue1, ingredientName2 -> ingredientValue2))

  /**
    * This sets three input ingredient to a set value. In this case the ingredients wont be taken from the runtime recipe.
    *
    * @param ingredientName1  the name of the first ingredient
    * @param ingredientValue1 the value of first the ingredient
    * @param ingredientName2  the name of the second ingredient
    * @param ingredientValue2 the value of second the ingredient
    * @param ingredientName3  the name of third the ingredient
    * @param ingredientValue3 the value of third the ingredient
    * @return
    */
  def withPredefinedIngredients(ingredientName1: String,
                                ingredientValue1: AnyRef,
                                ingredientName2: String,
                                ingredientValue2: AnyRef,
                                ingredientName3: String,
                                ingredientValue3: AnyRef): Interaction =
    addPredefinedIngredient(
      Map(ingredientName1 -> ingredientValue1,
        ingredientName2 -> ingredientValue2,
        ingredientName3 -> ingredientValue3))

  /**
    * This sets input ingredients to set values. In this case the ingredients wont be taken from the runtime recipe.
    *
    * @param newPredefinedIngredients The map containing ingredientName and ingredientValue for ingredients you want to set
    * @return
    */
  def withPredefinedIngredients(newPredefinedIngredients: java.util.Map[String, AnyRef]): Interaction =
    addPredefinedIngredient(newPredefinedIngredients.asScala.toMap)

  private def addPredefinedIngredient(params: Map[String, AnyRef]): Interaction =
    copy(predefinedIngredients = predefinedIngredients ++ params.map{case (key, value) => key -> Converters.toValue(value)})

  /**
    * This renames a input ingredient
    *
    * @param name the name of the input ingredient you want to rename
    * @param toName the new name for the ouput ingredient
    * @return
    */
  def renameRequiredIngredient(name: String,
                               toName: String): Interaction =
    copy(renamedInputIngredients = renamedInputIngredients + (name -> toName))

  /**
    * This renames the given input ingredients
    *
    * @param newOverriddenIngredients a map containing old and new names for input ingredients
    * @return new InteractionDescriptor with new ingredient names
    */
  def renameRequiredIngredients(newOverriddenIngredients: java.util.Map[String, String]): Interaction = {
    copy(renamedInputIngredients = renamedInputIngredients ++ newOverriddenIngredients.asScala.toMap)
  }

  def withEventTransformation(eventClazz: Class[_],
                              newEventName: String,
                              ingredientRenames: java.util.Map[String, String]): Interaction = {
    withEventTransformation(eventClazz, newEventName, ingredientRenames.asScala.toMap)
  }

  def withEventTransformation(eventClazz: Class[_],
                              newEventName: String): Interaction = {
    withEventTransformation(eventClazz, newEventName, Map.empty[String, String])
  }

  private def withEventTransformation(eventClazz: Class[_],
                                      newEventName: String,
                                      ingredientRenames: Map[String, String]): Interaction = {

    val originalEvent: javadsl.Event = javadsl.Event.fromClass(eventClazz, None)

    if (!output.contains(originalEvent))
      throw new RecipeValidationException(s"Event transformation given for Interaction $name but does not fire event ${originalEvent.name}")

    val eventOutputTransformer = EventOutputTransformer(newEventName, ingredientRenames)
    this.copy(eventOutputTransformers = eventOutputTransformers + (originalEvent -> eventOutputTransformer))
  }

  def withFailureStrategy(interactionFailureStrategy: InteractionFailureStrategy): Interaction = {
    this.copy(failureStrategy = Some(interactionFailureStrategy))
  }

  /**
    * Sets the maximum amount of times this interaction can be fired.
    *
    * @param times maximum amount of times this interaction can be fired
    * @return
    */
  def withMaximumInteractionCount(times: Int): Interaction =
    this.copy(maximumExecutionCount = Some(times))


  def withName(newName: String): Interaction = copy(name = newName, originalName = Some(originalName.getOrElse(name)))

  def withRequiredEvent(event: Event): Interaction = copy(requiredEvents = requiredEvents + event.name)

  def withRequiredEvents(events: Set[Event]): Interaction = copy(requiredEvents = requiredEvents ++ events.map(_.name))

  def withRequiredOneOfEvents(newRequiredOneOfEvents: Set[Event]): Interaction = {
    if (newRequiredOneOfEvents.nonEmpty && newRequiredOneOfEvents.size < 2)
      throw new IllegalArgumentException("At least 2 events should be provided as 'requiredOneOfEvents'")

    val newRequired: Set[Set[String]] = requiredOneOfEvents + newRequiredOneOfEvents.map(_.name)

    copy(requiredOneOfEvents = newRequired)
  }

  def withPredefinedIngredients(values: (String, Any)*): Interaction =
    withPredefinedIngredients(values.toMap)

  def withPredefinedIngredients(data: Map[String, Any]): Interaction =
    copy(predefinedIngredients = predefinedIngredients ++ data.map{case (key, value) => key -> Converters.toValue(value)})

  def withOverriddenIngredientName(oldIngredient: String,
                                   newIngredient: String): Interaction =
    copy(renamedInputIngredients = renamedInputIngredients + (oldIngredient -> newIngredient))

  def withEventOutputTransformer(event: Event, ingredientRenames: Map[String, String]): Interaction =
    copy(eventOutputTransformers = eventOutputTransformers + (event -> EventOutputTransformer(event.name, ingredientRenames)))

  def withEventOutputTransformer(event: Event, newEventName: String, ingredientRenames: Map[String, String]): Interaction =
    copy(eventOutputTransformers = eventOutputTransformers + (event -> EventOutputTransformer(newEventName, ingredientRenames)))


}

object Interaction {

  private val interactionMethodName: String = "apply"

  def apply(interactionClass: Class[_], newName: Option[String]): Interaction = {

    val name: String = interactionClass.getSimpleName

    val applyMethod: Method = interactionClass.getDeclaredMethods
      .find(_.getName == interactionMethodName)
      .getOrElse(throw new IllegalStateException(
        s"No method named '$interactionMethodName' defined on '${interactionClass.getName}'"))

    val inputIngredients: Seq[Ingredient] =
      applyMethod.getParameterNames.map(name =>
        Ingredient(name,
          ReflectionHelpers.parseType(
            applyMethod.parameterTypeForName(name).get,
            s"Unsupported type for ingredient '$name' on interaction '${interactionClass.getName}'")))

    def getOutputClasses(): Seq[Class[_]] = {

      def autoDetectOutput(): Seq[Class[_]] = {

        import scala.collection.JavaConverters._

        val returnType = applyMethod.getReturnType

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

      if (applyMethod.isAnnotationPresent(classOf[annotations.FiresEvent])) {

        val outputEventClasses: Seq[Class[_]] = applyMethod.getAnnotation(classOf[annotations.FiresEvent]).oneOf()

        if (outputEventClasses.isEmpty) {
          autoDetectOutput()
        }
        else {
          outputEventClasses.foreach {
            eventClass =>
              if (!applyMethod.getReturnType.isAssignableFrom(eventClass))
                throw new RecipeValidationException(s"Interaction $name provides event '${eventClass.getName}' that is incompatible with it's return type")
          }

          outputEventClasses
        }
      }
      else autoDetectOutput()
    }

    val output: Seq[Event] = getOutputClasses().map(javadsl.Event.fromClass(_, None))

    val originalName: Option[String] = newName match {
      case None => Some(name)
      case _    => None
    }

    Interaction(newName.getOrElse(name), inputIngredients, output, originalName, Set.empty, Set.empty, Map.empty, Map.empty, None, None, Map.empty)
  }

  def of[T](interactionClass: Class[T]): Interaction = apply(interactionClass, None)

  def of[T](interactionClass: Class[T], name: String): Interaction = apply(interactionClass, Some(name))
}
