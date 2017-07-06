package com.taboola.cronyx;

import java.util.Map;

public class Immediate extends TriggerDefinition {
    public enum MisfireInstruction {DROP, FIRE_NOW}

    private MisfireInstruction misfireInstruction;

    protected Immediate(NameAndGroup key, String description, NameAndGroup jobKey, Map<String, Object> triggerData, MisfireInstruction misfireInstruction) {
        super(key, description, jobKey, triggerData);
        this.misfireInstruction = misfireInstruction;
    }

    /**
     * private no-arg constructor for deserialization purposes
     */
    private Immediate() {
        super(null, null, null, null);
    }

    public MisfireInstruction getMisfireInstruction() {
        return misfireInstruction;
    }

    @Override
    public String toString() {
        String stringRep = super.toString();
        if (misfireInstruction != null) {
            stringRep += ", misfire handling policy: " + misfireInstruction;
        }
        return stringRep;
    }
}