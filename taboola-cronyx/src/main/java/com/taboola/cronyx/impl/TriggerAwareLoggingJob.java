package com.taboola.cronyx.impl;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.MDC;

public class TriggerAwareLoggingJob implements Job {

    private Job job;

    public TriggerAwareLoggingJob(Job job) {
        this.job = job;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        MDC.put("trigger", context.getTrigger().getKey().toString());

        job.execute(context);

        MDC.clear();
    }
}
