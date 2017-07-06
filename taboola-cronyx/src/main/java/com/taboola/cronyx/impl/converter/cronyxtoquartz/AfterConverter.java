package com.taboola.cronyx.impl.converter.cronyxtoquartz;

import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.JobDataMap;
import org.quartz.Trigger;

import com.taboola.cronyx.After;
import com.taboola.cronyx.Constants;
import com.taboola.cronyx.impl.quartz.ondemand.OnDemandScheduleBuilder;

public class AfterConverter implements CronyxToQuartzConverter<After> {

    @Override
    public Trigger convert(After trigger) {
        OnDemandScheduleBuilder onDemandScheduleBuilder = OnDemandScheduleBuilder.onDemandSchedule();

        JobDataMap dataMap = new JobDataMap(trigger.getTriggerData());
        dataMap.put(Constants.CRONYX_TYPE, After.class);

        return newTrigger()
                .withIdentity(trigger.getTriggerKey().getName(), trigger.getTriggerKey().getGroup())
                .forJob(trigger.getJobKey().getName(), trigger.getJobKey().getGroup())
                .withDescription(trigger.getDescription())
                .withSchedule(onDemandScheduleBuilder)
                .usingJobData(dataMap)
                .build();
    }

}
