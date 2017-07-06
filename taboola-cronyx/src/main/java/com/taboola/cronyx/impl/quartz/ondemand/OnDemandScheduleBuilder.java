package com.taboola.cronyx.impl.quartz.ondemand;

import org.quartz.ScheduleBuilder;
import org.quartz.spi.MutableTrigger;

public class OnDemandScheduleBuilder extends ScheduleBuilder<OnDemandTrigger> {

    private static final int MISFIRE_INSTRUCTION_FIRE_NOW = 1;

    public static OnDemandScheduleBuilder onDemandSchedule() {
        return new OnDemandScheduleBuilder();
    }

    @Override
    protected MutableTrigger build() {
        OnDemandTrigger onDemandTrigger = new OnDemandTrigger();
        onDemandTrigger.setMisfireInstruction(MISFIRE_INSTRUCTION_FIRE_NOW);
        return onDemandTrigger;
    }
}
