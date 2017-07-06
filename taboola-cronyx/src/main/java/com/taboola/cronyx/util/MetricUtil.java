package com.taboola.cronyx.util;

import com.taboola.cronyx.NameAndGroup;
import com.taboola.cronyx.TriggerDefinition;

public class MetricUtil {
    public static String createLastExecutionMetricName(TriggerDefinition trigger) {
        NameAndGroup jobKey = trigger.getJobKey();
        NameAndGroup triggerKey = trigger.getTriggerKey();
        String jobGroup = jobKey.getGroup() != null ? jobKey.getGroup().replace('.', '-') : "null";
        String jobName = jobKey.getName() != null ? jobKey.getName().replace('.', '-') : "null";
        String triggerGroup = triggerKey.getGroup() != null ? triggerKey.getGroup().replace('.', '-') : "null";
        String triggerName = triggerKey.getName() != null ? triggerKey.getName().replace('.', '-') : "null";
        return jobGroup + "-" + jobName + "." + triggerGroup + "-" + triggerName + ".lastSuccessfulExecution";
    }
}
