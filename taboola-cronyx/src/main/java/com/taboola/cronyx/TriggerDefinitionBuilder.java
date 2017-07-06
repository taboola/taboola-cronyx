package com.taboola.cronyx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TriggerDefinitionBuilder<T extends TriggerDefinitionBuilder> {

    protected NameAndGroup triggerKey;

    protected String description;

    protected NameAndGroup jobKey;

    protected Map<String, Object> triggerData = new HashMap<>();

    public static CronBuilder cron() {
        return new CronBuilder();
    }

    public static ImmediateBuilder immediate() {
        return new ImmediateBuilder();
    }

    public static AfterBuilder after() {
        return new AfterBuilder();
    }

    public T identifiedAs(NameAndGroup key) {
        this.triggerKey = key;
        return (T) this;
    }

    public T withDescription(String description) {
        this.description = description;
        return (T) this;
    }

    public T forJob(NameAndGroup jobKey) {
        this.jobKey = jobKey;
        return (T) this;
    }

    public T withData(Map<String, Object> triggerData) {
        if (triggerData != null) {
            this.triggerData.putAll(triggerData);
        }
        return (T) this;
    }

    public T withData(String key, Object value) {
        triggerData.put(key, value);
        return (T) this;
    }

    public abstract TriggerDefinition build();


    public static class CronBuilder extends TriggerDefinitionBuilder<CronBuilder> {
        private String cronExpression;

        private Cron.MisfireInstruction misfireInstruction;

        public CronBuilder withCronExpression(String cronExpression) {
            this.cronExpression = cronExpression;
            return this;
        }

        public CronBuilder withMisfireInstruction(Cron.MisfireInstruction misfireInstruction) {
            this.misfireInstruction = misfireInstruction;
            return this;
        }

        @Override
        public Cron build() {
            return new Cron(triggerKey, description, jobKey, triggerData, cronExpression, misfireInstruction);
        }
    }

    public static class ImmediateBuilder extends TriggerDefinitionBuilder<ImmediateBuilder> {
        private Immediate.MisfireInstruction misfireInstruction;

        public ImmediateBuilder withMisfireInstruction(Immediate.MisfireInstruction misfireInstruction) {
            this.misfireInstruction = misfireInstruction;
            return this;
        }

        @Override
        public Immediate build() {
            return new Immediate(triggerKey, description, jobKey, triggerData, misfireInstruction);
        }
    }

    public static class AfterBuilder extends TriggerDefinitionBuilder<AfterBuilder> {
        private List<NameAndGroup> previousTriggers;

        public AfterBuilder withPreviousTriggers(List<NameAndGroup> previousTriggers) {
            this.previousTriggers = previousTriggers;
            return this;
        }

        @Override
        public After build() {
            return new After(triggerKey, description, jobKey, triggerData, previousTriggers);
        }
    }

}
