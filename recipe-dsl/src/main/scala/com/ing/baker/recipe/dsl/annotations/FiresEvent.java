package com.ing.baker.recipe.dsl.annotations;

import javax.inject.Qualifier;
import java.lang.annotation.*;

@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FiresEvent {
    Class<?>[] oneOf() default { };
}
