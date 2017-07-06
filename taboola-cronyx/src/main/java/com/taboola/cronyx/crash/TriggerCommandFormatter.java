package com.taboola.cronyx.crash;

import java.util.List;
import java.util.Map;

import com.taboola.cronyx.After;
import com.taboola.cronyx.Cron;
import com.taboola.cronyx.Immediate;
import com.taboola.cronyx.NameAndGroup;
import com.taboola.cronyx.TriggerDefinition;

/**
 * This class is used to format a valid CRaSH command string out of the specified {@code TriggerDefinition}
 * The result is a string that one could input into the Cronyx CLI and schedule the given trigger
 */
public class TriggerCommandFormatter {
    public String format(TriggerDefinition triggerDef) {
        StringBuilder sb = new StringBuilder("trigger");

        if (triggerDef instanceof Cron) {
            Cron cron = (Cron) triggerDef;
            sb.append(" cron");
            appendMisfireInstruction(sb, cron.getMisfireInstruction());
            appendNameAndGroup(sb, cron.getTriggerKey());
            appendNameAndGroup(sb, cron.getJobKey());
            appendCronExpression(sb, cron);
            appendJobArguments(sb, cron.getTriggerData());
        } else if (triggerDef instanceof Immediate) {
            Immediate immediate = (Immediate) triggerDef;
            sb.append(" now");
            appendMisfireInstruction(sb, immediate.getMisfireInstruction());
            sb.append(" -n");
            appendNameAndGroup(sb, immediate.getTriggerKey());
            appendNameAndGroup(sb, immediate.getJobKey());
            appendJobArguments(sb, immediate.getTriggerData());
        } else if (triggerDef instanceof After) {
            After after = (After) triggerDef;
            sb.append(" after");
            appendNameAndGroup(sb, after.getTriggerKey());
            appendNameAndGroup(sb, after.getJobKey());
            appendCommaSeparatedKeys(sb, after.getPreviousTriggers());
            appendJobArguments(sb, after.getTriggerData());
        } else {
            return null;
        }

        return sb.toString();
    }

    private void appendNameAndGroup(StringBuilder sb, NameAndGroup jobKey) {
        sb.append(" ");
        sb.append(jobKey);
    }

    private void appendCronExpression(StringBuilder sb, Cron cron) {
        sb.append(" \"");
        sb.append(cron.getCronExpression());
        sb.append("\"");
    }

    private void appendMisfireInstruction(StringBuilder sb, Enum instruction) {
        if (instruction != null) {
            sb.append(" -m ");
            sb.append(instruction);
        }
    }

    private void appendJobArguments(StringBuilder sb, Map<String, Object> jobArguments) {
        jobArguments.entrySet()
                    .stream()
                    .filter(entry -> !entry.getKey().startsWith("_"))
                    .forEach(entry -> sb.append(" " + entry.getKey() + "=" + entry.getValue()));
    }

    private void appendCommaSeparatedKeys(StringBuilder sb, List<NameAndGroup> triggerKeys) {
        sb.append(" ");
        triggerKeys.forEach(key -> sb.append(key).append(","));
        sb.deleteCharAt(sb.length() - 1);
    }
}
