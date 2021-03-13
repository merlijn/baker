package com.ing.baker.runtime.core
import com.ing.baker.il.IngredientDescriptor
import com.ing.baker.il.petrinet.InteractionTransition
import com.ing.baker.types.*

import scala.util.Random

class DummyInteractionImpl(i: InteractionTransition) extends InteractionImplementation {

  override def execute(input: Seq[(String, Value)]): Option[ProcessEvent] = {

    if (i.events.isEmpty)
      None
    else {
      val n = Random.nextInt(i.events.size)
      val e = i.events(n)

      val ingredients = e.ingredients.map {
        case IngredientDescriptor(name, ingredientType) => name -> Value.generate(ingredientType)
      }

      Some(ProcessEvent(e.name, ingredients))
    }
  }

  override val inputTypes: Seq[Type] = i.requiredIngredients.map(_.`type`)

  override val name: String = i.originalName
}
