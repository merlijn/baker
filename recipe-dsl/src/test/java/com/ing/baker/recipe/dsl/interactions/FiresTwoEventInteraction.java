package com.ing.baker.recipe.dsl.interactions;

import com.ing.baker.recipe.annotations.FiresEvent;
import com.ing.baker.recipe.dsl.events.InteractionEventExample;
import com.ing.baker.recipe.dsl.events.InteractionProvidedEvent;
import com.ing.baker.recipe.dsl.events.InteractionProvidedEvent2;

import javax.inject.Named;

public interface FiresTwoEventInteraction {
    @FiresEvent
    InteractionEventExample apply(@Named("initialIngredient") String initialIngredient);
}

