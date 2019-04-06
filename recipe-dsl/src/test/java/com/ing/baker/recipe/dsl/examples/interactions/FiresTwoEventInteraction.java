package com.ing.baker.recipe.dsl.examples.interactions;

import com.ing.baker.recipe.annotations.FiresEvent;
import com.ing.baker.recipe.dsl.examples.events.InteractionEventExample;

import javax.inject.Named;

public interface FiresTwoEventInteraction {
    @FiresEvent
    InteractionEventExample apply(@Named("initialIngredient") String initialIngredient);
}

