package com.taboola.cronyx;

import java.util.List;
import java.util.Map;

public class After extends TriggerDefinition {

    private List<NameAndGroup> previousTriggers;

    protected After(NameAndGroup triggerKey, String description, NameAndGroup jobKey, Map<String, Object> triggerData, List<NameAndGroup> previousTriggers) {
        super(triggerKey, description, jobKey, triggerData);
        this.previousTriggers = previousTriggers;
    }

    public List<NameAndGroup> getPreviousTriggers() {
        return previousTriggers;
    }

    public void setPreviousTriggers(List<NameAndGroup> previousTriggers) {
        this.previousTriggers = previousTriggers;
    }

    @Override
    public String toString() {
        String prevString = previousTriggers == null || previousTriggers.isEmpty() ? "" : previousTriggers.toString();
        return super.toString() + ", previous trigger: \"" + prevString + "\"";
    }
}
