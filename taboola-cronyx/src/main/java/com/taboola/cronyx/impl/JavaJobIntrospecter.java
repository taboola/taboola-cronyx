package com.taboola.cronyx.impl;

import com.taboola.cronyx.ArgumentDefinition;
import com.taboola.cronyx.JobDefinition;
import com.taboola.cronyx.JobType;
import com.taboola.cronyx.NameAndGroup;
import com.taboola.cronyx.annotations.*;
import com.taboola.cronyx.exceptions.BadJobDefinitionException;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JavaJobIntrospecter implements JobIntrospecter {
    @Override
    public JobDefinition getJobDefinitionForInstance(Class<?> jobClazz) {
        boolean allowConcurrentExecution = isConcurrentExecutionAllowed(jobClazz);
        boolean isRecoverable = isJobRecoverable(jobClazz);
        boolean isRetryable = isJobRetryable(jobClazz);
        Job jobAnnotation = AnnotationUtils.findAnnotation(jobClazz, Job.class);
        NameAndGroup jobKey = extractJobKey(jobAnnotation, jobClazz);
        String description = jobAnnotation.description();
        JobType jobType = JobType.JAVA;
        Method jobMethod = findJobMethod(jobClazz);
        ArgumentDefinition[] argumentDefinitions = extractJobArguments(jobMethod);

        return new JobDefinition(jobKey, description, jobClazz, jobType, argumentDefinitions, jobMethod.getName(),
                allowConcurrentExecution, isRecoverable, isRetryable);
    }

    protected NameAndGroup extractJobKey(Job jobAnnotation, Class<?> jobClazz){

        if (jobAnnotation == null) {
            throw new BadJobDefinitionException("Job class must be marked with @com.taboola.cronyx.annotations.Job");
        }

        String name;
        String group;

        if (StringUtils.hasText(jobAnnotation.group())) {
            group = jobAnnotation.group();
        } else {
            group = DEFAULT_GROUP;
        }

        if (StringUtils.hasText(jobAnnotation.value())) {
            name = jobAnnotation.value();
        } else {
            name = jobClazz.getSimpleName();
        }

        return new NameAndGroup(name, group);
    }

    protected ArgumentDefinition[] extractJobArguments(Method method){
        Class<?>[] paramTypes = method.getParameterTypes();
        ArgumentDefinition[] args = new ArgumentDefinition[paramTypes.length];

        for (int i = 0; i<paramTypes.length; i++) {
            MethodParameter methodParam = new MethodParameter(method, i);
            args[i] = extractSingleArgumentDefinition(methodParam);
        }

        return args;
    }

    private ArgumentDefinition extractSingleArgumentDefinition(MethodParameter methodParameter){
        Class<?> type = methodParameter.getParameterType();
        List<Annotation> argumentDescriptors =
                Arrays.stream(methodParameter.getParameterAnnotations())
                      .filter(anno -> AnnotationUtils.isAnnotationMetaPresent(anno.getClass(), JobArgument.class))
                      .collect(Collectors.toList());
        if (argumentDescriptors.size() != 1) {
            throw new BadJobDefinitionException("a job argument has to be annotated with exactly one @JobArgument meta-annotated annotation");
        }
        return new ArgumentDefinition(type, argumentDescriptors.get(0));
    }

    protected Method findJobMethod(Class<?> clazz){
        List<Method> jobMethods =
                Arrays.stream(clazz.getMethods())
                      .filter(method -> method.isAnnotationPresent(JobMethod.class))
                      .collect(Collectors.toList());
        if (jobMethods.size() != 1) {
            throw new BadJobDefinitionException("a cronyx job must have exactly one @JobMethod annotated method");
        }
        return jobMethods.get(0);

    }

    private boolean isJobRecoverable(Class<?> clazz) {
        Recoverable recoverable = AnnotationUtils.findAnnotation(clazz, Recoverable.class);
        return (recoverable != null);
    }

    private boolean isJobRetryable(Class<?> clazz) {
        return !clazz.isAnnotationPresent(NonRetryable.class);
    }

    private static boolean isConcurrentExecutionAllowed(Class<?> jobClazz) {
        return AnnotationUtils.findAnnotation(jobClazz, NonConcurrent.class) == null;
    }

}