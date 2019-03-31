package com.ing.baker.runtime.core

import com.ing.baker.il.petrinet.InteractionTransition
import com.ing.baker.types.{Type, Value}

class JavaScriptInteractionImpl(i: InteractionTransition, script: String) extends InteractionImplementation {

  override val name: String = i.originalName

  override val inputTypes: Seq[Type] = i.requiredIngredients.map(_.`type`)

  def eventFn(name: String) =
    s"""
      | function $name(json) {
      |    return JSON.stringify( {
      |       event: "$name",
      |       ingredients: json
      |    });
      | }
    """.stripMargin

  val scriptPre = i.originalEvents.map(_.name).map(eventFn).mkString("\n")

  val fullScript = scriptPre + script

  override def execute(input: Seq[(String, Value)]): Option[ProcessEvent] = {

    import javax.script.ScriptEngineManager

    val engine = new ScriptEngineManager().getEngineByMimeType("text/javascript")

    val bindings = engine.createBindings()

    input.foreach {
      case (name, value) => bindings.put(name, value)
    }

    val result = engine.eval(fullScript)

    println(result)

    None
  }
}
