package com.taboola.cronyx.impl.converter.cronyxtoquartz;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobDataMap;
import org.quartz.Trigger;

import com.taboola.cronyx.Cron;

public class CronConverter implements CronyxToQuartzConverter<Cron> {

    @Override
    public Trigger convert(Cron trigger) {
        CronScheduleBuilder schedule = cronSchedule(trigger.getCronExpression());
        if (trigger.getMisfireInstruction() == Cron.MisfireInstruction.DROP) {
            schedule.withMisfireHandlingInstructionDoNothing();
        } else if (trigger.getMisfireInstruction() == Cron.MisfireInstruction.FIRE_ONCE) {
            schedule.withMisfireHandlingInstructionFireAndProceed();
        }

        return newTrigger()
                .forJob(trigger.getJobKey().getName(), trigger.getJobKey().getGroup())
                .withDescription(trigger.getDescription())
                .withIdentity(trigger.getTriggerKey().getName(), trigger.getTriggerKey().getGroup())
                .withSchedule(schedule)
                .usingJobData(new JobDataMap(trigger.getTriggerData()))
                .build();
    }

}
