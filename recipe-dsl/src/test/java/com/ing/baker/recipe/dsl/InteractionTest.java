package com.ing.baker.recipe.dsl;

import com.ing.baker.recipe.dsl.examples.events.InteractionProvidedEvent;
import com.ing.baker.recipe.dsl.examples.events.InteractionProvidedEvent2;
import com.ing.baker.recipe.dsl.examples.events.SensoryEventWithIngredient;
import com.ing.baker.recipe.dsl.examples.events.SensoryEventWithoutIngredient;
import com.ing.baker.recipe.dsl.examples.interactions.FiresEventInteraction;
import com.ing.baker.recipe.dsl.examples.interactions.SimpleInteraction;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import scala.Option;

import static com.ing.baker.recipe.dsl.Interaction.reflect;
import static org.junit.Assert.*;

public class InteractionTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldCreateInteractionDescriptorWithDefaultName() {
        Interaction id = Interaction.reflect(SimpleInteraction.class);
        assertEquals("SimpleInteraction", id.name());
        assertEquals(id.output().size(), 1);
        assertEquals(id.output().apply(0), Event.reflect(SimpleInteraction.InitialIngredientEvent.class, Option.empty()));
    }

    @Test
    public void shouldCreateInteractionDescriptorWithChangedName() {
        Interaction id = reflect(SimpleInteraction.class, "ChangedName");
        assertEquals("ChangedName", id.name());
    }

    @Test
    public void shouldUpdateTheRequiredEventListFromClass() {
        Interaction id = Interaction.reflect(SimpleInteraction.class);
        assertTrue(id.requiredEvents().isEmpty());

        Interaction idWithRequiredEvent =
                id.withRequiredEvents(Event.reflect(SensoryEventWithIngredient.class));

        assertEquals(idWithRequiredEvent.requiredEvents().size(), 1);
        assertTrue(idWithRequiredEvent.requiredEvents().contains("SensoryEventWithIngredient"));

        Interaction idWithRequiredEvents =
                id.withRequiredEvents(
                        Event.reflect(SensoryEventWithIngredient.class),
                        Event.reflect(SensoryEventWithoutIngredient.class));

        assertEquals(idWithRequiredEvents.requiredEvents().size(), 2);
        assertTrue(idWithRequiredEvents.requiredEvents().contains("SensoryEventWithIngredient"));
        assertTrue(idWithRequiredEvents.requiredEvents().contains("SensoryEventWithoutIngredient"));
    }

    @Test
    public void shouldUpdateTheRequiredOneOfEventListFromClass() {
        Interaction id = Interaction.reflect(SimpleInteraction.class);
        assertTrue(id.requiredOneOfEvents().isEmpty());

        Interaction idWithRequiredOneOfEvents =
                id.withRequiredOneOfEvents(
                        Event.reflect(SensoryEventWithIngredient.class),
                        Event.reflect(SensoryEventWithoutIngredient.class));

        assertEquals(idWithRequiredOneOfEvents.requiredOneOfEvents().head().size(), 2);
        assertTrue(idWithRequiredOneOfEvents.requiredOneOfEvents().head().contains("SensoryEventWithIngredient"));
        assertTrue(idWithRequiredOneOfEvents.requiredOneOfEvents().head().contains("SensoryEventWithoutIngredient"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectUsingLessThanTwoOneOfRequiredEvents() {
        Interaction id = Interaction.reflect(SimpleInteraction.class);
        assertTrue(id.requiredOneOfEvents().isEmpty());

        id.withRequiredOneOfEvents(Event.reflect(SensoryEventWithIngredient.class));
    }

    @Test
    public void shouldUpdateTheMaximumInteractionCount() {
        Interaction id = Interaction.reflect(SimpleInteraction.class);
        assertTrue(id.maximumExecutionCount().isEmpty());

        Interaction idWithMaximumInteractionCount =
                id.withMaximumInteractionCount(1);
        assertTrue(idWithMaximumInteractionCount.maximumExecutionCount().isDefined());
        assertEquals(idWithMaximumInteractionCount.maximumExecutionCount().get(), 1);

        idWithMaximumInteractionCount =
                idWithMaximumInteractionCount.withMaximumInteractionCount(2);
        assertTrue(idWithMaximumInteractionCount.maximumExecutionCount().isDefined());
        assertEquals(idWithMaximumInteractionCount.maximumExecutionCount().get(), 2);
    }

    //TODO add tests for all InteractionDescriptor methods
    //predefinedIngredients
    //overriddenIngredientNames
    //failureStrategy
    //eventOutputTransformers
}
