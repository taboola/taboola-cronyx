package com.taboola.cronyx.annotations;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;


/**
 * This annotation is used to indicate that this class is an implementation of a "job".
 * In addition the annotation can be used to identify the job unique name (id) and a human readable description
 * (for UI and human interaction)
 *
 * The annotation includes the spring <code>@Component</code> as a meta-annotation, which means that classes annotated
 * with <code>@Job</code> will also inherit spring component behaviours such as being a candidate for auto-detection,
 * <code>*Aware</code> interface processing, and spring annotation processing.
 *
 * The annotation is also <code>@Inherited</code>, which means that classes which extend a class marked with this
 * annotation will inherit will also become a job, without having to be marked with the annotation as well.
 *
 * @see com.taboola.cronyx.annotations.JobMethod
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public @interface Job {

    /**
     * The value may indicate a suggestion for a logical job name,
     * to be turned into a Spring bean in case of an autodetected component.
     * @return the suggested job name, if any
     */
    @AliasFor(attribute = "name")
    String value() default "";

    //@todo: check if it is possible to use the name as an alias to value.
    @AliasFor(attribute = "value")
    String name() default "";


    /**
     * @return optional value to indicate a grouping of the current job
     */
    String group() default "DEFAULT";
    /**
     * @return A human readable description for the job, will be used for human interaction.
     */
    String description() default "";

}
