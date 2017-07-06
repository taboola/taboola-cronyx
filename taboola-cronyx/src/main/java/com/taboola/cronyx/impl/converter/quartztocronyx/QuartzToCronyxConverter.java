package com.taboola.cronyx.impl.converter.quartztocronyx;

import org.quartz.Trigger;

import com.taboola.cronyx.TriggerDefinition;

public interface QuartzToCronyxConverter<T extends Trigger> {

    TriggerDefinition convert(T trigger);
}
