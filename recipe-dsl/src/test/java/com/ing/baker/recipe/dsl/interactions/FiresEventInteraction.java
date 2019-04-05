package com.ing.baker.recipe.dsl.interactions;

import com.ing.baker.recipe.annotations.FiresEvent;
import com.ing.baker.recipe.dsl.events.InteractionProvidedEvent;

import javax.inject.Named;

public interface FiresEventInteraction {
    @FiresEvent(oneOf = InteractionProvidedEvent.class)
    InteractionProvidedEvent apply(@Named("initialIngredient") String initialIngredient);
}

