package com.taboola.cronyx.annotations;

import java.lang.annotation.*;

/**
 * This annotation is used to mark a Cronyx job as one that the service should try to recover in-case of a failed execution attempt.
 * For example, if a {@code @Recoverable} annotated job is fired and fails to execute properly it will be marked for another execution attempt.
 * Another execution will then occur regardless of the job's trigger scheduling
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface Recoverable {
}
