package com.taboola.cronyx;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.taboola.cronyx.NameAndGroup;
import com.taboola.cronyx.rest.TriggerDefinitionJsonDeserializer;

import java.util.Map;

@JsonDeserialize(using = TriggerDefinitionJsonDeserializer.class)
public abstract class TriggerDefinition {
    public static final String DEFAULT_TRIGGER_GROUP = "default.trigger.group";

    private NameAndGroup triggerKey;

    private String description;

    private NameAndGroup jobKey;

    private Map<String, Object> triggerData;

    public TriggerDefinition(NameAndGroup triggerKey, String description, NameAndGroup jobKey, Map<String, Object> triggerData) {
        this.triggerKey = triggerKey;
        this.description = description;
        this.jobKey = jobKey;
        this.triggerData = triggerData;
    }

    public NameAndGroup getTriggerKey() {
        return triggerKey;
    }

    public String getDescription() {
        return description;
    }

    public NameAndGroup getJobKey() {
        return jobKey;
    }

    public Map<String, Object> getTriggerData() {
        return triggerData;
    }

    public static NameAndGroup randomTriggerKey() {
        return new NameAndGroup("trigger_" + System.nanoTime(), DEFAULT_TRIGGER_GROUP);
    }

    @Override
    public String toString() {
        String str =  getTriggerKey() + " for job: " + getJobKey();
        if (getDescription()!= null && !getDescription().isEmpty()) {
            str += ", description: " + getDescription();
        }
        if (getTriggerData() != null && !getTriggerData().isEmpty()) {
            str += ", job arguments: " + getTriggerData();
        }
        return str;
    }
}
