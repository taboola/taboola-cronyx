package com.taboola.cronyx;

import java.util.Map;

public class Cron extends TriggerDefinition {
    public enum MisfireInstruction {DROP, FIRE_ONCE}

    private String cronExpression;

    private MisfireInstruction misfireInstruction;


    protected Cron(NameAndGroup key, String description, NameAndGroup jobKey, Map<String, Object> triggerData,
                   String cronExpression, MisfireInstruction misfireInstruction) {
        super(key, description, jobKey, triggerData);
        this.cronExpression = cronExpression;
        this.misfireInstruction = misfireInstruction;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public MisfireInstruction getMisfireInstruction() {
        return misfireInstruction;
    }

    @Override
    public String toString() {
        String str = super.toString() + ", cron expression: \"" + cronExpression + "\"";
        if (misfireInstruction != null) {
            str += ", misfire handling policy: " + misfireInstruction;
        }
        return str;
    }
}
