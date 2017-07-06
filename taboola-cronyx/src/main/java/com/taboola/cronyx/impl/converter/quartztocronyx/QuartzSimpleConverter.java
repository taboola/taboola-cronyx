package com.taboola.cronyx.impl.converter.quartztocronyx;

import static com.taboola.cronyx.TriggerDefinitionBuilder.immediate;
import static org.quartz.Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY;

import org.quartz.SimpleTrigger;

import com.taboola.cronyx.Immediate;
import com.taboola.cronyx.NameAndGroup;
import com.taboola.cronyx.TriggerDefinition;

public class QuartzSimpleConverter implements QuartzToCronyxConverter<SimpleTrigger> {

    @Override
    public TriggerDefinition convert(SimpleTrigger quartzTrigger) {
        return immediate()
                .identifiedAs(new NameAndGroup(quartzTrigger.getKey().getName(), quartzTrigger.getKey().getGroup()))
                .forJob(new NameAndGroup(quartzTrigger.getJobKey().getName(), quartzTrigger.getJobKey().getGroup()))
                .withDescription(quartzTrigger.getDescription())
                .withData(quartzTrigger.getJobDataMap())
                .withMisfireInstruction(quartzTrigger.getMisfireInstruction() == MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY ?
                        Immediate.MisfireInstruction.DROP :
                        Immediate.MisfireInstruction.FIRE_NOW)
                .build();
    }
}
