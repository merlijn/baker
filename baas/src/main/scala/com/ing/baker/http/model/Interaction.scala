package com.ing.baker.http.model

case class Interaction(name: String,
                       originalName: Option[String],
                       input: Seq[Ingredient],
                       output: Seq[Event],
                       maximumExecutionCount: Option[Int] = None)
