package com.ing.baker.recipe.dsl.examples.interactions;

import com.ing.baker.recipe.dsl.annotations.FiresEvent;
import com.ing.baker.recipe.dsl.examples.events.InteractionProvidedEvent;

import javax.inject.Named;

public interface FiresEventInteraction {
    @FiresEvent(oneOf = InteractionProvidedEvent.class)
    InteractionProvidedEvent apply(@Named("initialIngredient") String initialIngredient);
}

