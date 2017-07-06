package com.taboola.cronyx;

import java.io.Serializable;

public class JobDefinition implements Serializable {

    private NameAndGroup key;

    private String description;

    private Class<?> implementingClass;

    private JobType jobType;

    private String methodName;

    private ArgumentDefinition[] args;

    private boolean allowConcurentExecution;

    private boolean isRecoverable;

    private boolean isRetryable;

    protected JobDefinition() {
    }

    public JobDefinition(NameAndGroup key, String description, Class<?> implementingClass, JobType jobType,
                         ArgumentDefinition[] args, String methodName, boolean allowConcurentExecution,
                         boolean isRecoverable, boolean isRetryable) {
        this.key = key;
        this.description = description;
        this.implementingClass = implementingClass;
        this.jobType = jobType;
        this.methodName = methodName;
        this.args = args;
        this.isRecoverable = isRecoverable;
        this.isRetryable = isRetryable;
        this.allowConcurentExecution = allowConcurentExecution;
    }

    public NameAndGroup getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }

    public Class<?> getImplementingClass() {
        return implementingClass;
    }

    public JobType getJobType() {
        return jobType;
    }

    public boolean isRecoverable() {
        return isRecoverable;
    }

    public boolean isRetryable() {
        return isRetryable;
    }

    public String getMethodName() {
        return methodName;
    }

    public ArgumentDefinition[] getArgs() {
        return args;
    }

    public boolean isConcurrentExecutionAllowed() {
        return allowConcurentExecution;
    }
}