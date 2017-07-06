package com.taboola.cronyx.impl.converter.cronyxtoquartz;

import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.JobDataMap;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;

import com.taboola.cronyx.Immediate;

public class ImmediateConverter implements CronyxToQuartzConverter<Immediate> {

    @Override
    public Trigger convert(Immediate trigger) {
        SimpleScheduleBuilder simpleSchedule = SimpleScheduleBuilder.simpleSchedule();
        if (trigger.getMisfireInstruction() == Immediate.MisfireInstruction.DROP) {
            simpleSchedule = simpleSchedule.withMisfireHandlingInstructionIgnoreMisfires();
        } else if (trigger.getMisfireInstruction() == Immediate.MisfireInstruction.FIRE_NOW) {
            simpleSchedule = simpleSchedule.withMisfireHandlingInstructionFireNow();
        }
        return newTrigger()
                .withIdentity(trigger.getTriggerKey().getName(), trigger.getTriggerKey().getGroup())
                .forJob(trigger.getJobKey().getName(), trigger.getJobKey().getGroup())
                .withDescription(trigger.getDescription())
                .withSchedule(simpleSchedule)
                .usingJobData(new JobDataMap(trigger.getTriggerData()))
                .startNow()
                .build();
    }

}
