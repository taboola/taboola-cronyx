package com.taboola.cronyx.impl;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.taboola.cronyx.Constants;
import com.taboola.cronyx.JobDefinition;
import com.taboola.cronyx.annotations.NonRetryable;

public class ErrorHandlingJob implements Job {

    public static final String NUMBER_OF_RETRIES_PARAM = "numRetries";
    public static final String SLEEP_TIME_BETWEEN_RETRIES_PARAM = "sleepRetries";

    private final int DEFAULT_NUMBER_OF_RETRIES = 2;
    private final long DEFAULT_SLEEP_TIME_BETWEEN_RETRIES = 1000;

    private Job job;

    public ErrorHandlingJob(Job job) {
        this.job = job;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        final int numberOfRetries;
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        numberOfRetries = jobDataMap.containsKey(NUMBER_OF_RETRIES_PARAM) ?
                jobDataMap.getIntValue(NUMBER_OF_RETRIES_PARAM) : DEFAULT_NUMBER_OF_RETRIES;
        //check if running job class has the "NonRetryable" annotation
        boolean isRetryable = jobDataMap.containsKey(Constants.JOB_DEFINITION) ?
                ((JobDefinition)jobDataMap.get(Constants.JOB_DEFINITION)).isRetryable() : true;
        try {
            job.execute(context);
            //reset retry param, just in case
            jobDataMap.putAsString(NUMBER_OF_RETRIES_PARAM, 0);
        } catch (Exception e) {
            //do not retry if job is not retryable or no more retries left
            if (!isRetryable || numberOfRetries <= 0) {
                throwAndFinish(jobDataMap, context, e);
            }
            triggerRefire(jobDataMap, numberOfRetries, context, e);
        } catch (Throwable t) {
            //do not retry on throwable
            throwAndFinish(jobDataMap, context, t);
        }
    }

    private void triggerRefire(JobDataMap jobDataMap, int numberOfRetries, JobExecutionContext context, Throwable t)
            throws JobExecutionException {
        final long sleepTimeBetweenRetries = jobDataMap.containsKey(SLEEP_TIME_BETWEEN_RETRIES_PARAM) ?
                jobDataMap.getLongValue(SLEEP_TIME_BETWEEN_RETRIES_PARAM) : DEFAULT_SLEEP_TIME_BETWEEN_RETRIES;
        try {
            Thread.sleep(sleepTimeBetweenRetries);
        } catch (InterruptedException e) {}
        context.put(Constants.JOB_EXCEPTION, t);
        jobDataMap.putAsString(NUMBER_OF_RETRIES_PARAM, --numberOfRetries);
        //set refire flag as true
        throw new JobExecutionException(t, true);
    }

    private void throwAndFinish(JobDataMap jobDataMap, JobExecutionContext context, Throwable t)
            throws JobExecutionException {
        context.put(Constants.JOB_EXCEPTION, t);
        //reset retry param, just in case
        jobDataMap.putAsString(NUMBER_OF_RETRIES_PARAM, 0);
        JobExecutionException e = new JobExecutionException("This trigger has thrown a terminal exception. " +
                "Retries exceeded or job not retryable", t);
        throw e;
    }

}