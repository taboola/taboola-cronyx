package com.taboola.cronyx.annotations;

import java.lang.annotation.*;

/**
 * This annotation is used to mark a JobMethod argument as one that should contain the scheduled fire time
 * of this instance of the Job. A job instance's scheduled fire time might differ from its actual fire time.
 * For example, a job may have been scheduled to run at 10:00 but was actually executed only at 10:02 due to resource limitations.
 *
 * @see com.taboola.cronyx.annotations.Job
 * @see com.taboola.cronyx.annotations.JobMethod
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@JobArgument
public @interface ScheduledTime {
}
