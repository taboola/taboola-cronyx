package com.taboola.cronyx;

import org.quartz.JobExecutionContext;

import java.util.Map;

public interface ArgumentResolver {
    Object resolve(ArgumentDefinition argumentDefinition, Map<String, ?> dataMap, JobExecutionContext context) throws ArgumentResolutionException;
}
