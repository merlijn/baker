package com.ing.baker.recipe.javadsl.interactions;

import com.ing.baker.recipe.annotations.FiresEvent;
import com.ing.baker.recipe.javadsl.events.InteractionProvidedEvent;

import javax.inject.Named;

public interface FiresEventInteraction {
    @FiresEvent(oneOf = InteractionProvidedEvent.class)
    InteractionProvidedEvent apply(@Named("initialIngredient") String initialIngredient);
}

