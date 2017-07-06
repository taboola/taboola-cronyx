package com.taboola.cronyx.impl.quartz.ondemand;

import org.quartz.impl.jdbcjobstore.SimplePropertiesTriggerPersistenceDelegateSupport;
import org.quartz.impl.jdbcjobstore.SimplePropertiesTriggerProperties;
import org.quartz.spi.OperableTrigger;

public class OnDemandDelegateSupport extends SimplePropertiesTriggerPersistenceDelegateSupport {

    private static final String ON_DEMAND = "ONDEMAND";

    @Override
    protected SimplePropertiesTriggerProperties getTriggerProperties(OperableTrigger trigger) {

        return new SimplePropertiesTriggerProperties();
    }

    /**
     *  names of columns must match setters on class onDemandTrigger
     */
    @Override
    protected TriggerPropertyBundle getTriggerPropertyBundle(SimplePropertiesTriggerProperties properties) {
        return new TriggerPropertyBundle(OnDemandScheduleBuilder.onDemandSchedule(), new String[0], new Object[0]);
    }

    @Override
    public boolean canHandleTriggerType(OperableTrigger trigger) {
        return (trigger instanceof OnDemandTrigger);
    }

    /**
     * trigger type is currently limited to 8 chars (table QRTZ_TRIGGERS)
     */
    @Override
    public String getHandledTriggerTypeDiscriminator() {
        return ON_DEMAND;
    }

}
