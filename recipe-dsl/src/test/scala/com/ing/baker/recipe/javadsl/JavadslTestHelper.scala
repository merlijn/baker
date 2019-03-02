package com.ing.baker.recipe.javadsl

object JavadslTestHelper {

  //Ingredients
  val initialIngredientCheck: Ingredient = Ingredient[String]("initialIngredient")
  val ProcessIdStringCheck: Ingredient = Ingredient[String]("$ProcessId$")
  //Events
  val interactionProvidedEventCheck: Event = new Event("InteractionProvidedEvent", Seq.empty, None)
  val interactionProvidedEvent2Check: Event = new Event("InteractionProvidedEvent2", Seq.empty, None)
  val sensoryEventWithIngredientCheck: Event = new Event("SensoryEventWithIngredient", Seq(initialIngredientCheck), Some(1))
  val sensoryEventWithoutIngredientCheck: Event = new Event("SensoryEventWithoutIngredient", Seq.empty, Some(1))

  //Interactions
  val requiresProcessIdStringInteractionCheck: Interaction = Interaction("RequiresProcessIdStringInteraction", Seq(ProcessIdStringCheck, initialIngredientCheck), Seq.empty)
  val firesEventInteractionCheck: Interaction = Interaction("FiresEventInteraction", Seq(initialIngredientCheck), Seq((interactionProvidedEventCheck)))
  val firesTwoEventInteractionCheck: Interaction = Interaction("FiresTwoEventInteraction", Seq(initialIngredientCheck), Seq(interactionProvidedEventCheck, interactionProvidedEvent2Check))
}
