package com.taboola.cronyx.impl;

import com.taboola.cronyx.JobDefinition;

public interface JobIntrospecter {

    public static String DEFAULT_GROUP = "DEFAULT";

    JobDefinition getJobDefinitionForInstance(Class<?> jobClazz);


}
