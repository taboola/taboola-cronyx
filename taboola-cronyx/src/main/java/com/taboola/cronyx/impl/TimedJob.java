package com.taboola.cronyx.impl;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.quartz.*;

import static com.codahale.metrics.Timer.Context;

public class TimedJob implements Job {

    private Job job;
    private MetricRegistry registry;

    public TimedJob(Job job, MetricRegistry registry) {
        this.job = job;
        this.registry = registry;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Context okTime = getTimer(context, "ok").time();
        Context errorTime = getTimer(context, "error").time();
        Context totalTime = getTimer(context, "total").time();

        try {
            job.execute(context);
            okTime.stop();
        } catch (Throwable t) {
            errorTime.stop();
            throw t;
        } finally {
            totalTime.stop();
        }
    }

    private Timer getTimer(JobExecutionContext context, String suffix) {
        return registry.timer(getMetricPrefix(context) + ".executionTime." + suffix);
    }

    private static String getMetricPrefix(JobExecutionContext context) {
        JobKey jobKey = context.getJobDetail().getKey();
        TriggerKey triggerKey = context.getTrigger().getKey();
        String jobGroup = jobKey.getGroup() != null ? jobKey.getGroup().replace('.', '-') : "null";
        String jobName = jobKey.getName() != null ? jobKey.getName().replace('.', '-') : "null";
        String triggerGroup = triggerKey.getGroup() != null ? triggerKey.getGroup().replace('.', '-') : "null";
        String triggerName = triggerKey.getName() != null ? triggerKey.getName().replace('.', '-') : "null";
        return jobGroup + "-" + jobName + "." + triggerGroup + "-" + triggerName;
    }
}
