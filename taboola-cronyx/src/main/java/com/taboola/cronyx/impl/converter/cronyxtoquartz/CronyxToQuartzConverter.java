package com.taboola.cronyx.impl.converter.cronyxtoquartz;

import org.quartz.Trigger;

import com.taboola.cronyx.TriggerDefinition;

public interface CronyxToQuartzConverter<T extends TriggerDefinition> {

    Trigger convert(T triggerDefinition);
}
