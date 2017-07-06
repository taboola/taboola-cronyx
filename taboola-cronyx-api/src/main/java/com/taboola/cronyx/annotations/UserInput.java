package com.taboola.cronyx.annotations;

import java.lang.annotation.*;

/**
 * This annotation marks a Cronyx job argument as one that needs to be set by the user
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@JobArgument
public @interface UserInput {

    /**
     * @return name of the parameter, none if the original name is to be used.
     */
    String name();

    /**
     * @return A human readable description for the argument, will be used for human interaction.
     */
    String description() default "";

    /**
     * @return default value to use in the case that no external value was provided.
     */
    String defaultValue() default "";


}
