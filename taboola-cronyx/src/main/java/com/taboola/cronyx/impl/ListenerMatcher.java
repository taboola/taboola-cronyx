package com.taboola.cronyx.impl;

import com.taboola.cronyx.After;
import com.taboola.cronyx.Cron;
import com.taboola.cronyx.CronyxExecutionContext;
import com.taboola.cronyx.FiringListener;
import com.taboola.cronyx.NameAndGroup;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Predicate;

import static com.taboola.cronyx.impl.ExecutionStatus.*;

/**
 * This class is used to determine for which events should a {@code FiringListener} be invoked.
 * {@code ListenerMatcher} implements {@code Predicate} and can thus be chained with other {@code ListenerMatcher}s
 * using {@code Predicate.or} and {@code Predicate.and} to produce more complex logic
 * @see Predicate
 * @see FiringListener
 */
public interface ListenerMatcher {

    boolean test(CronyxExecutionContext ctx);

    default ListenerMatcher and(ListenerMatcher other) {
        Objects.requireNonNull(other);
        return ctx -> test(ctx) && other.test(ctx);
    }

    default ListenerMatcher or(ListenerMatcher other) {
        Objects.requireNonNull(other);
        return ctx -> test(ctx) || other.test(ctx);
    }

    /**
     *
     * @return A matcher matching any trigger
     */
    static ListenerMatcher anyTrigger() {
        return ctx -> ctx != null;
    }

    /**
     *
     * @return A matcher matching triggers for a job in the specified group
     */
    static ListenerMatcher jobInGroup(String jobGroup) {
        return ctx -> ctx.getFiredJob().getKey().getGroup().equals(jobGroup);
    }

    /**
     *
     * @return A matcher matching triggers for a job whose key is equal to the specified key
     */
    static ListenerMatcher jobWithKey(NameAndGroup key) {
        return ctx -> ctx.getFiredJob().getKey().getName().equals(key.getName()) &&
                ctx.getFiredJob().getKey().getGroup().equals(key.getGroup());
    }

    /**
     *
     * @return A matcher matching any trigger firing event
     */
    static ListenerMatcher jobFired() {
        return ctx -> ctx.getCurrentStatus() == FIRED;
    }

    /**
     *
     * @return A matcher matching any job that was completed successfully
     */
    static ListenerMatcher jobCompletedSuccessfully() {
        return ctx -> ctx.getCurrentStatus() == COMPLETED_SUCCESSFULLY;
    }

    /**
     *
     * @return A matcher matching any job that threw an exception
     */
    static ListenerMatcher jobCompletedWithException() {
        return ctx -> ctx.getCurrentStatus() == COMPLETED_WITH_EXCEPTION;
    }

    /**
     *
     * @return A matcher matching any job that threw an exception of the specified type
     */
    static ListenerMatcher jobCompletedWithException(Class<? extends Throwable> exceptionType) {
        return ctx ->
                ctx.getCurrentStatus() == COMPLETED_WITH_EXCEPTION &&
                        exceptionType.equals(ctx.getExecutionException().getClass());
    }

    /**
     *
     * @return A matcher matching a firing of a trigger in the specified group
     */
    static ListenerMatcher triggerInGroup(String triggerGroup) {
        return ctx -> ctx.getFiredTrigger().getTriggerKey().getGroup().equals(triggerGroup);
    }

    /**
     *
     * @return A matcher matching a firing of a trigger with the specified key
     */
    static ListenerMatcher triggerWithKey(NameAndGroup key) {
        return ctx -> ctx.getFiredTrigger().getTriggerKey().getName().equals(key.getName()) &&
                ctx.getFiredTrigger().getTriggerKey().getGroup().equals(key.getGroup());
    }

    /**
     *
     * @return A matcher matching a firing whose actual execution time exceeded its scheduled execution
     * by at least the specified duration
     */
    static ListenerMatcher executionExceededScheduleBy(Duration duration) {
        return ctx -> Duration.between(ctx.getScheduledFireTime(), ctx.getActualFireTime()).compareTo(duration) >= 0;
    }

    /**
     *
     * @return A matcher matching any trigger that has been refired
     */
    static ListenerMatcher triggerRefired() {
        return ctx -> ctx.getFiringAttemptNumber() > 1;
    }

    static ListenerMatcher cronTrigger() {
        return ctx -> ctx.getFiredTrigger() instanceof Cron;
    }

    static ListenerMatcher afterTrigger() {
        return ctx -> ctx.getFiredTrigger() instanceof After;
    }
}