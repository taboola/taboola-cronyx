package com.taboola.cronyx.impl.converter.quartztocronyx;

import static com.taboola.cronyx.TriggerDefinitionBuilder.cron;
import static org.quartz.CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING;

import org.quartz.CronTrigger;

import com.taboola.cronyx.Cron;
import com.taboola.cronyx.NameAndGroup;
import com.taboola.cronyx.TriggerDefinition;

public class QuartzCronConverter implements QuartzToCronyxConverter<CronTrigger> {

    @Override
    public TriggerDefinition convert(CronTrigger quartzTrigger) {
        return cron()
                .identifiedAs(new NameAndGroup(quartzTrigger.getKey().getName(), quartzTrigger.getKey().getGroup()))
                .forJob(new NameAndGroup(quartzTrigger.getJobKey().getName(), quartzTrigger.getJobKey().getGroup()))
                .withDescription(quartzTrigger.getDescription())
                .withData(quartzTrigger.getJobDataMap())
                .withCronExpression(quartzTrigger.getCronExpression())
                .withMisfireInstruction(quartzTrigger.getMisfireInstruction() == MISFIRE_INSTRUCTION_DO_NOTHING ?
                        Cron.MisfireInstruction.DROP :
                        Cron.MisfireInstruction.FIRE_ONCE)
                .build();
    }
}
