package com.taboola.cronyx.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark a {@code JobArgument} as one that should be wired in the trigger execution phase
 * by a user-input resource
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@JobArgument
public @interface SpringQualifier {
    String beanSource();
    String defaultBeanName() default "";
}
