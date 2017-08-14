package com.taboola.cronyx.annotations;


import java.lang.annotation.*;

/**
 * This annotation is used to mark the job method, on classes marked with <code>@Job</code> that have more than one
 * method. It can only be placed on a class marked with the <code>@Job</code> param, otherwise it will be ignored.
 *
 * @see com.taboola.cronyx.annotations.Job
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface JobMethod {

    String name() default "job";

}
