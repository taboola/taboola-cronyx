package com.taboola.cronyx.annotations;

import java.lang.annotation.*;

/**
 * This annotation is used to add metadata to a paramater on a <code>@JobMethod</code> method.
 * The argument value marked by this annotation will be populated by the previous return value of this job. 
 * Only one argument can be marked with this annotation per job method, and the type of the argument must be identical 
 * to the return type of the marked method, and must be serializable. 
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@JobArgument
public @interface JobPreviousValue {
}
