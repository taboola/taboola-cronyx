package com.taboola.cronyx;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.taboola.cronyx.impl.ExecutionStatus;

public class CronyxExecutionContext {

    private final String contextKey;

    private final JobDefinition firedJob;

    private final TriggerDefinition firedTrigger;

    /**
     * firingAttemptNumber == 1 for the first attempt
     */
    private volatile int firingAttemptNumber;

    private final Instant scheduledFireTime;

    private final Instant actualFireTime;

    private final Map<String, Object> mergedDataMap;

    /**
     * This map can be used to pass information between different listener invocations on the same firing instance.
     * For example, on a {@code FIRED} event one could put an object into the {@code contextualDataMap} to be used
     * later when a {@code COMPLETED_SUCCESSFULLY} event has been invoked for this firing
     */
    private final ConcurrentMap<String, Object> contextualDataMap;

    private volatile Object executionResult;

    private volatile Throwable executionException;

    private volatile ExecutionStatus currentStatus;

    public CronyxExecutionContext(String contextKey, JobDefinition firedJob, TriggerDefinition firedTrigger, int firingAttemptNumber, Instant scheduledFireTime, Instant actualFireTime,
                                  Map<String, Object> mergedDataMap, Object executionResult, Throwable executionException, ExecutionStatus currentStatus) {
        this.contextKey = contextKey;
        this.firedJob = firedJob;
        this.firedTrigger = firedTrigger;
        this.firingAttemptNumber = firingAttemptNumber;
        this.scheduledFireTime = scheduledFireTime;
        this.actualFireTime = actualFireTime;
        this.mergedDataMap = Collections.unmodifiableMap(mergedDataMap);
        this.contextualDataMap = new ConcurrentHashMap<>();
        this.executionResult = executionResult;
        this.executionException = executionException;
        this.currentStatus = currentStatus;
    }

    private CronyxExecutionContext(ContextBuilder builder) {
        this.contextKey = builder.contextKey;
        this.firedJob = builder.firedJob;
        this.firedTrigger = builder.firedTrigger;
        this.firingAttemptNumber = builder.firingAttemptNumber;
        this.scheduledFireTime = builder.scheduledFireTime;
        this.actualFireTime = builder.actualFireTime;
        this.mergedDataMap = builder.mergedDataMap;
        this.contextualDataMap = builder.contextualDataMap;
        this.executionResult = builder.executionResult;
        this.executionException = builder.executionException;
        this.currentStatus = builder.currentStatus;
    }

    public String getContextKey() {
        return contextKey;
    }

    public JobDefinition getFiredJob() {
        return firedJob;
    }

    public TriggerDefinition getFiredTrigger() {
        return firedTrigger;
    }

    public int getFiringAttemptNumber() {
            return firingAttemptNumber;
    }

    public Instant getScheduledFireTime() {
        return scheduledFireTime;
    }

    public Instant getActualFireTime() {
        return actualFireTime;
    }

    public Map<String, Object> getMergedDataMap() {
        return mergedDataMap;
    }

    public Object getExecutionResult() {
            return executionResult;
    }

    public Throwable getExecutionException() {
            return executionException;
    }

    public ExecutionStatus getCurrentStatus() {
            return currentStatus;
    }

    public Object get(String key) {
        return contextualDataMap.get(key);
    }

    public void put(String key, Object value) {
        contextualDataMap.put(key, value);
    }

    public void putAll(Map<String, Object> additional) {
        contextualDataMap.putAll(additional);
    }

    public void setFiringAttemptNumber(int firingAttemptNumber) {
            this.firingAttemptNumber = firingAttemptNumber;
    }

    public void setExecutionResult(Object executionResult) {
            this.executionResult = executionResult;
    }

    public void setExecutionException(Throwable executionException) {
            this.executionException = executionException;

    }

    public void setCurrentStatus(ExecutionStatus currentStatus) {
            this.currentStatus = currentStatus;
    }

    public static class ContextBuilder {
        private String contextKey;
        private JobDefinition firedJob;
        private TriggerDefinition firedTrigger;
        private int firingAttemptNumber = 1;
        private Instant scheduledFireTime;
        private Instant actualFireTime;
        private Map<String, Object> mergedDataMap = new HashMap<>();
        private ConcurrentMap<String, Object> contextualDataMap = new ConcurrentHashMap<>();
        private Object executionResult;
        private Throwable executionException;
        private ExecutionStatus currentStatus;

        public ContextBuilder setContextKey(String contextKey) {
            this.contextKey = contextKey;
            return this;
        }

        public ContextBuilder setFiredJob(JobDefinition firedJob) {
            this.firedJob = firedJob;
            return this;
        }

        public ContextBuilder setFiredTrigger(TriggerDefinition firedTrigger) {
            this.firedTrigger = firedTrigger;
            return this;
        }

        public ContextBuilder setFiringAttemptNumber(int firingAttemptNumber) {
            this.firingAttemptNumber = firingAttemptNumber;
            return this;
        }

        public ContextBuilder setScheduledFireTime(Instant scheduledFireTime) {
            this.scheduledFireTime = scheduledFireTime;
            return this;
        }

        public ContextBuilder setActualFireTime(Instant actualFireTime) {
            this.actualFireTime = actualFireTime;
            return this;
        }

        public ContextBuilder setMergedDataMap(Map<String, Object> mergedDataMap) {
            this.mergedDataMap = mergedDataMap;
            return this;
        }

        public ContextBuilder setContextualDataMap(ConcurrentMap<String, Object> contextualDataMap) {
            this.contextualDataMap = contextualDataMap;
            return this;
        }

        public ContextBuilder setExecutionResult(Object executionResult) {
            this.executionResult = executionResult;
            return this;
        }

        public ContextBuilder setExecutionException(Throwable executionException) {
            this.executionException = executionException;
            return this;
        }

        public ContextBuilder setCurrentStatus(ExecutionStatus currentStatus) {
            this.currentStatus = currentStatus;
            return this;
        }

        public CronyxExecutionContext build() {
            return new CronyxExecutionContext(this);
        }
    }
}