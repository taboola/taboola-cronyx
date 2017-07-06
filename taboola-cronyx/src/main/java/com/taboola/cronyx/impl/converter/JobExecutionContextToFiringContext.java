package com.taboola.cronyx.impl.converter;

import static com.taboola.cronyx.Constants.CONTEXT_KEY;
import static com.taboola.cronyx.Constants.JOB_DEFINITION;
import static com.taboola.cronyx.Constants.JOB_EXCEPTION;
import static com.taboola.cronyx.impl.ExecutionStatus.FIRED;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.quartz.JobExecutionContext;

import com.taboola.cronyx.CronyxExecutionContext;
import com.taboola.cronyx.JobDefinition;
import com.taboola.cronyx.TriggerDefinition;
import com.taboola.cronyx.impl.converter.quartztocronyx.QuartzToCronyxSelector;

public class JobExecutionContextToFiringContext {

    private QuartzToCronyxSelector quartzToCronyxSelector;

    public JobExecutionContextToFiringContext(QuartzToCronyxSelector quartzToCronyxSelector) {
        this.quartzToCronyxSelector = quartzToCronyxSelector;
    }

    public CronyxExecutionContext convert(JobExecutionContext context) {
        String contextKey = context.get(CONTEXT_KEY).toString();
        JobDefinition jobDef = (JobDefinition) context.getMergedJobDataMap().get(JOB_DEFINITION);
        TriggerDefinition triggerDef = quartzToCronyxSelector.convert(context.getTrigger());
        Instant scheduledTime = context.getScheduledFireTime().toInstant();
        Instant actualTime = context.getFireTime().toInstant();
        Map<String, Object> jobData = new HashMap<>(context.getMergedJobDataMap());
        Throwable jobException = (Throwable) context.get(JOB_EXCEPTION);
        Object jobResult = context.getResult();
        int attemptNumber = context.getRefireCount() + 1;
        return new CronyxExecutionContext(contextKey, jobDef, triggerDef, attemptNumber, scheduledTime,
                actualTime, jobData, jobResult, jobException, FIRED);
    }
}
