package com.ing.baker.runtime

import com.ing.baker.il.petrinet.InteractionTransition
import com.ing.baker.recipe.scaladsl.{Event, Ingredient, Interaction}
import com.ing.baker.runtime.core.{InteractionImplementation, ProcessEvent}
import com.ing.baker.types.Converters.toJava
import com.ing.baker.types.{Converters, Type, Value}

import scala.reflect.runtime.universe.TypeTag

/**
  *  This class is for wiring the scala DSL to the runtime components (interaction implementations).
  *
  */
object ScalaDSLRuntime {

  class ScalaInteractionImplementation(i: Interaction, fn: Seq[Value] => ProcessEvent) extends InteractionImplementation {

    override val name: String = i.name

    override val inputTypes: Seq[Type] = i.input.map(_.ingredientType)

    override def execute(input: Seq[Value]): Option[ProcessEvent] = Some(fn(input))
  }

  // TODO use shapeless to abstract over function arity and add type safety
  implicit class InteractionOps(i: Interaction) {

    def implement[A : TypeTag](fn: A => ProcessEvent): InteractionImplementation =
      new ScalaInteractionImplementation(i, { input =>
        fn(toJava[A](input(0)))
      })

    def implement[A : TypeTag, B : TypeTag](fn: (A, B) => ProcessEvent): InteractionImplementation =
      new ScalaInteractionImplementation(i, { input =>
        fn(toJava[A](input(0)), toJava[B](input(1)))
      })

    def implement[A : TypeTag, B : TypeTag, C : TypeTag](fn: (A, B, C) => ProcessEvent): InteractionImplementation =
      new ScalaInteractionImplementation(i, { input =>
        fn(toJava[A](input(0)), toJava[B](input(1)), toJava[C](input(2)))
      })
  }

  implicit class EventOps(e: Event) {
    def instance(values: Any*): ProcessEvent = {

      val providedIngredients: Seq[(String, Value)] =  e.providedIngredients.map(_.name).zip(values.toSeq.map(Converters.toValue))

      ProcessEvent(e.name, providedIngredients)
    }
  }

  implicit object IngredientMap {

    def apply(values: (Ingredient[_], Any)*): Map[String, Value] = {
      values.map { case (key, value) => key.name -> Converters.toValue(value)
      }.toMap
    }
  }
}
