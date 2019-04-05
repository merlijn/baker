package com.ing.baker.recipe.dsl.interactions;

import javax.inject.Named;

public interface SimpleInteraction {

    class InitialIngredientEvent {
        final String initialIngredient;
        public InitialIngredientEvent(String initialIngredient) {
            this.initialIngredient = initialIngredient;
        }
    }

    InitialIngredientEvent apply(@Named("initialIngredient") String initialIngredient);
}
