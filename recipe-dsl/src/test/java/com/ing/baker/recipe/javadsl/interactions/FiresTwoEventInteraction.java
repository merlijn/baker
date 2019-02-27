package com.ing.baker.recipe.javadsl.interactions;

import com.ing.baker.recipe.annotations.FiresEvent;
import com.ing.baker.recipe.javadsl.events.InteractionEventExample;
import com.ing.baker.recipe.javadsl.events.InteractionProvidedEvent;
import com.ing.baker.recipe.javadsl.events.InteractionProvidedEvent2;

import javax.inject.Named;

public interface FiresTwoEventInteraction {
    @FiresEvent
    InteractionEventExample apply(@Named("initialIngredient") String initialIngredient);
}

