package com.taboola.cronyx.impl.converter.quartztocronyx;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.quartz.JobDataMap;

import com.taboola.cronyx.After;
import com.taboola.cronyx.Constants;
import com.taboola.cronyx.NameAndGroup;
import com.taboola.cronyx.TriggerDefinition;
import com.taboola.cronyx.TriggerDefinitionBuilder;
import com.taboola.cronyx.impl.quartz.ondemand.OnDemandTrigger;

public class QuartzOnDemandConverter implements QuartzToCronyxConverter<OnDemandTrigger> {


    private static final Map<Class, Supplier<TriggerDefinitionBuilder>> builderMap = new HashMap<Class, Supplier<TriggerDefinitionBuilder>>() {{
        put(After.class, TriggerDefinitionBuilder::after);
    }};

    @Override
    public TriggerDefinition convert(OnDemandTrigger quartzTrigger) {

        JobDataMap dataMap = quartzTrigger.getJobDataMap();
        Class clz = (Class) dataMap.get(Constants.CRONYX_TYPE);

        return builderMap.get(clz).get()
                .identifiedAs(new NameAndGroup(quartzTrigger.getKey().getName(), quartzTrigger.getKey().getGroup()))
                .forJob(new NameAndGroup(quartzTrigger.getJobKey().getName(), quartzTrigger.getJobKey().getGroup()))
                .withDescription(quartzTrigger.getDescription())
                .withData(quartzTrigger.getJobDataMap())
                .build();
    }
}
