package com.taboola.cronyx.rest;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taboola.cronyx.Cron;
import com.taboola.cronyx.Immediate;
import com.taboola.cronyx.NameAndGroup;
import com.taboola.cronyx.TriggerDefinition;
import com.taboola.cronyx.TriggerDefinitionBuilder;

import java.io.IOException;
import java.util.Map;

import static java.util.Collections.emptyMap;

public class TriggerDefinitionJsonDeserializer extends JsonDeserializer<TriggerDefinition> {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public TriggerDefinition deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode root = p.getCodec().readTree(p);
        JsonNode triggerKey = root.get("triggerKey");
        JsonNode description = root.get("description");
        JsonNode jobKey = root.get("jobKey");
        JsonNode triggerData = root.get("triggerData");
        JsonNode misfireInstruction = root.get("misfireInstruction");
        JsonNode cron = root.get("cronExpression");

        if (triggerKey == null || jobKey == null) {
            throw new IOException("JSON is missing a triggerKey and jobKey");
        }

        if (cron == null) {
            return TriggerDefinitionBuilder.immediate()
                                           .identifiedAs(mapper.convertValue(triggerKey, NameAndGroup.class))
                                           .forJob(mapper.convertValue(jobKey, NameAndGroup.class))
                                           .withDescription(description != null ? description.textValue() : null)
                                           .withData(triggerData != null ? mapper.convertValue(triggerData, Map.class) : emptyMap())
                                           .withMisfireInstruction(misfireInstruction != null ? mapper.convertValue(misfireInstruction, Immediate.MisfireInstruction.class) : null)
                                           .build();
        } else {
            return TriggerDefinitionBuilder.cron()
                                    .identifiedAs(mapper.convertValue(triggerKey, NameAndGroup.class))
                                    .forJob(mapper.convertValue(jobKey, NameAndGroup.class))
                                    .withCronExpression(cron.textValue())
                                    .withDescription(description != null ? description.textValue() : null)
                                    .withData(triggerData != null ? mapper.convertValue(triggerData, Map.class) : emptyMap())
                                    .withMisfireInstruction(misfireInstruction != null ? mapper.convertValue(misfireInstruction, Cron.MisfireInstruction.class) : null)
                                    .build();
        }
    }
}
