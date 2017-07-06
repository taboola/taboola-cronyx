package com.taboola.cronyx.util;

import com.taboola.cronyx.JobDefinition;
import org.quartz.Job;

@FunctionalInterface
public interface QuartzJobClassSelector {
    Class<? extends Job> select(JobDefinition details);
}
