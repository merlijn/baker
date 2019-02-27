package com.ing.baker.recipe.annotations;

import javax.inject.Qualifier;
import java.lang.annotation.*;

@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Deprecated
public @interface RequiresIngredient {
    String value() default "";
}
