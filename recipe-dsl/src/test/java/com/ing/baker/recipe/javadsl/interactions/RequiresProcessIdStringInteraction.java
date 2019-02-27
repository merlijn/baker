package com.ing.baker.recipe.javadsl.interactions;

import com.ing.baker.recipe.annotations.ProcessId;

import javax.inject.Named;

public interface RequiresProcessIdStringInteraction {
    String apply(@ProcessId String processId, @Named("initialIngredient") String initialIngredient);
}
