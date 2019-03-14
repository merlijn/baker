package com.ing.baker.recipe.javadsl;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import scala.Some;

import java.time.Duration;

import static org.junit.Assert.assertEquals;


public class InteractionFailureStrategyTest {

    public static class ExampleEvent { }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldSetupRetryWithIncrementalBackoff() {
        InteractionFailureStrategy.RetryWithIncrementalBackoff retryWithIncrementalBackoff = new InteractionFailureStrategy.RetryWithIncrementalBackoffBuilder()
                .withInitialDelay(Duration.ofMillis(100))
                .withMaximumRetries(10)
                .build();

        assertEquals(100, retryWithIncrementalBackoff.initialDelay().toMillis());
        assertEquals(10, retryWithIncrementalBackoff.maximumRetries());

    }

    @Test
    public void shouldGiveErrorIfNoInitialDelay() {
        exception.expect(IllegalArgumentException.class);
        new InteractionFailureStrategy.RetryWithIncrementalBackoffBuilder()
                .withMaximumRetries(10)
                .build();
    }

    @Test
    public void shouldGiveErrorIfNoDeadlineOrMaxRetries() {
        exception.expect(IllegalArgumentException.class);
        new InteractionFailureStrategy.RetryWithIncrementalBackoffBuilder()
                .withInitialDelay(Duration.ofMillis(100))
                .build();
    }

    @Test
    public void shouldSetupFiresEventWithClass() {

        InteractionFailureStrategy.FireEventAfterFailure eventName =
                InteractionFailureStrategy.fireEvent("Foo");

        assertEquals(eventName.eventName(), Some.apply("Foo"));

        InteractionFailureStrategy.FireEventAfterFailure clazz =
                InteractionFailureStrategy.fireEvent(ExampleEvent.class);

        assertEquals(clazz.eventName(), Some.apply("ExampleEvent"));
    }
}
