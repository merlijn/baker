//package com.ing.baker.recipe.dsl.examples
//
//import com.ing.baker.recipe.dsl._
//
//import scala.concurrent.duration.DurationInt
//
///** This recipe is meant for testing purposes.
//  *
//  * If you need a recipe that makes more sense then see the example package
//  *
//  * @see com.ing.baker.Examples
//  */
//object AllTypeRecipe {
//
//  case class Payload(data: Map[String, String], userData: Map[String, java.lang.Integer])
//
//  // ingredients
//
//  val bigPayloadIngredient = Ingredient[Payload]("bigPayloadIngredient")
//  val javaBooleanIngredient = Ingredient[java.lang.Boolean]("javaBooleanIngredient")
//  val javaByteIngredient = Ingredient[java.lang.Byte]("javaByteIngredient")
//  val javaShortIngredient = Ingredient[java.lang.Short]("javaShortIngredient")
//  val javaCharacterIngredient = Ingredient[java.lang.Character]("javaCharacterIngredient")
//  val javaIntegerIngredient = Ingredient[java.lang.Integer]("javaIntegerIngredient")
//  val javaLongIngredient = Ingredient[java.lang.Long]("javaLongIngredient")
//  val javaFloatIngredient = Ingredient[java.lang.Float]("javaFloatIngredient")
//  val javaDoubleIngredient = Ingredient[java.lang.Double]("javaDoubleIngredient")
//  val javaStringIngredient = Ingredient[java.lang.String]("javaStringIngredient")
//  val javaBigDecimalIngredient = Ingredient[java.math.BigDecimal]("javaBigDecimalIngredient")
//  val javaBigIntegerIngredient = Ingredient[java.math.BigInteger]("javaBigIntegerIngredient")
//  val byteArrayIngredient = Ingredient[Array[Byte]]("byteArrayIngredient")
//
//  val booleanIngredient = Ingredient[Boolean]("booleanIngredient")
//  val byteIngredient = Ingredient[Byte]("byteIngredient")
//  val shortIngredient = Ingredient[Short]("shortIngredient")
//  val charIngredient = Ingredient[Char]("charIngredient")
//  val intIngredient = Ingredient[Int]("intIngredient")
//  val longIngredient = Ingredient[Long]("longIngredient")
//  val floatIngredient = Ingredient[Float]("floatIngredient")
//  val doubleIngredient = Ingredient[Double]("doubleIngredient")
//  val stringIngredient = Ingredient[String]("stringIngredient")
//  val bigDecimalIngredient = Ingredient[BigDecimal]("bigDecimalIngredient")
//  val bigIntIngredient = Ingredient[BigInt]("bigIntIngredient")
//
//  val optionalIngredient = Ingredient[Option[String]]("optionalIngredient")
//  val optionalIngredientForNone = Ingredient[Option[String]]("optionalIngredientForNone")
//  val primitiveOptionalIngredient = Ingredient[Option[Int]]("primitiveOptionalIngredient")
//  val listIngredient = Ingredient[List[String]]("listIngredient")
//  val mapIngredient = Ingredient[Map[String, String]]("mapIngredient")
//  val mapIngredientWithPrimitives = Ingredient[Map[String, Int]]("mapIngredientWithPrimitives")
//  val mapIngredientWithBoxedTypes = Ingredient[Map[String, java.lang.Integer]]("mapIngredientWithBoxedTypes")
//
//  // events
//
//  val javaDataEvent = Event(
//    name = "javaDataEvent",
//    providedIngredients = Seq(
//      javaBooleanIngredient,
//      javaByteIngredient,
//      javaShortIngredient,
//      javaCharacterIngredient,
//      javaIntegerIngredient,
//      javaLongIngredient,
//      javaFloatIngredient,
//      javaDoubleIngredient,
//      javaStringIngredient,
//      javaBigDecimalIngredient,
//      javaBigIntegerIngredient
//    )
//  )
//
//  val scalaDataEvent = Event(
//    name = "scalaDataEvent",
//    providedIngredients = Seq(
//      booleanIngredient,
//      byteIngredient,
//      shortIngredient,
//      charIngredient,
//      intIngredient,
//      longIngredient,
//      floatIngredient,
//      doubleIngredient,
//      stringIngredient,
//      bigDecimalIngredient,
//      bigIntIngredient
//    )
//  )
//
//  val byteArrayEvent = Event(
//    name = "byteArrayEvent",
//    providedIngredients = Seq(byteArrayIngredient)
//  )
//
//  val bigPayloadEvent = Event(
//    name = "bigPayloadEvent",
//    providedIngredients = Seq(bigPayloadIngredient)
//  )
//
//  val otherEvent = Event(
//    name = "otherEvent",
//    providedIngredients = Seq(optionalIngredient, listIngredient)
//  )
//
//  val emptyEvent = Event(name = "emptyEvent", providedIngredients = Seq.empty)
//
//  val mapEvent = Event(
//    name = "mapEvent",
//    providedIngredients = Seq(mapIngredient, mapIngredientWithPrimitives, mapIngredientWithBoxedTypes)
//  )
//
//  // interactions
//
//  val interactionOne = Interaction(
//    name = "interactionOne",
//    input = Seq(bigPayloadIngredient),
//    output = Seq(javaDataEvent)
//  )
//
//  val interactionTwo = Interaction(
//    name = "interactionTwo",
//    input = Seq(javaBooleanIngredient, javaByteIngredient),
//    output = Seq(byteArrayEvent, otherEvent)
//  )
//
//  val interactionThree = Interaction(
//    name = "interactionThree",
//    input = Seq(bigPayloadIngredient, javaByteIngredient),
//    output = Seq(emptyEvent, otherEvent, mapEvent)
//  )
//
//  val interactionFive = Interaction(
//    name = "interactionFive",
//    input = Seq.empty,
//    output = Seq(emptyEvent)
//  )
//
//  val interactionSeven = Interaction(
//    name = "interactionSeven",
//    input = Seq(javaIntegerIngredient),
//    output = Seq(scalaDataEvent)
//  )
//
//  val allTypesInteraction = Interaction(
//    name = "allTypesInteraction",
//    input = Seq(bigPayloadIngredient, javaBooleanIngredient, javaByteIngredient, javaShortIngredient, javaCharacterIngredient, javaIntegerIngredient,
//      javaLongIngredient, javaFloatIngredient, javaDoubleIngredient, javaStringIngredient, javaBigDecimalIngredient, javaBigIntegerIngredient, byteArrayIngredient,
//      booleanIngredient, byteIngredient, shortIngredient, charIngredient, intIngredient,
//      longIngredient, floatIngredient, doubleIngredient, stringIngredient, bigDecimalIngredient, bigIntIngredient, optionalIngredient, optionalIngredientForNone,
//      primitiveOptionalIngredient, listIngredient, mapIngredient, mapIngredientWithPrimitives, mapIngredientWithBoxedTypes),
//    output = Seq(emptyEvent)
//  )
//
//  // recipe
//
//  val recipe =
//    Recipe("AllTypeRecipe")
//      .withInteractions(
//        interactionTwo.withFailureStrategy(InteractionFailureStrategy.RetryWithIncrementalBackoff.builder()
//          .withInitialDelay(5.seconds)
//          .withBackoffFactor(2.0)
//          .withDeadline(10.minutes)
//          .withMaxTimeBetweenRetries(100.milliseconds)
//          .withFireRetryExhaustedEvent("someEventName")
//          .build()),
//        interactionThree
//          .withEventOutputTransformer(otherEvent, "renamedOtherEvent", Map("optionalIngredient" -> "renamedOptionalIngredient"))
//          .withFailureStrategy(InteractionFailureStrategy.RetryWithIncrementalBackoff.builder()
//            .withInitialDelay(5.seconds)
//            .withBackoffFactor(2.0)
//            .withMaximumRetries(10)
//            .withMaxTimeBetweenRetries(100.milliseconds)
//            .withFireRetryExhaustedEvent(mapEvent.name)
//            .build()
//          )
//          .withMaximumInteractionCount(5)
//          .withOverriddenIngredientName("longIngredient", "renamedLongIngredient")
//          .withRequiredOneOfEvents(Set(mapEvent, otherEvent)),
//        interactionFive
//          .withRequiredEvents(byteArrayEvent)
//          .withFailureStrategy(InteractionFailureStrategy.FireEventAfterFailure()),
//        interactionSeven,
//        allTypesInteraction.withPredefinedIngredients(
//          bigPayloadIngredient(Payload(Map("stringKey" -> "stringValue"), Map("someOtherStringKey" -> java.lang.Integer.MAX_VALUE))),
//          javaBooleanIngredient(java.lang.Boolean.TRUE),
//          javaByteIngredient(java.lang.Byte.MAX_VALUE),
//          javaShortIngredient(java.lang.Short.MAX_VALUE),
//          javaCharacterIngredient(java.lang.Character.MAX_VALUE),
//          javaIntegerIngredient(java.lang.Integer.MAX_VALUE),
//          javaLongIngredient(java.lang.Long.MAX_VALUE),
//          javaFloatIngredient(java.lang.Float.MAX_VALUE),
//          javaDoubleIngredient(java.lang.Double.MAX_VALUE),
//          javaStringIngredient("Some String"),
//          javaBigDecimalIngredient(java.math.BigDecimal.valueOf(4.2)),
//          javaBigIntegerIngredient(java.math.BigInteger.TEN),
//          byteArrayIngredient("some byte array".getBytes),
//          booleanIngredient(true),
//          byteIngredient(Byte.MinValue),
//          shortIngredient(Short.MinValue),
//          charIngredient(Char.MinValue),
//          intIngredient(Int.MinValue),
//          longIngredient(Long.MinValue),
//          floatIngredient(Float.MinValue),
//          doubleIngredient(Double.MinValue),
//          stringIngredient(""),
//          bigDecimalIngredient(BigDecimal(1.2)),
//          bigIntIngredient(BigInt(Int.MaxValue)),
//          optionalIngredient(Some("some string")),
//          optionalIngredientForNone(None),
//          primitiveOptionalIngredient(Some(42)),
//          listIngredient(List("str1", "str2")),
//          mapIngredient(Map("key1" -> "value1")),
//          mapIngredientWithPrimitives(Map("key1" -> 42)),
//          mapIngredientWithBoxedTypes(Map("key1" -> Int.box(42)))
//        )
//      )
//      .withSensoryEvents(Set(bigPayloadEvent, mapEvent))
//      .withEventReceivePeriod(1 minute)
//      .withRetentionPeriod(5 minutes)
//}
