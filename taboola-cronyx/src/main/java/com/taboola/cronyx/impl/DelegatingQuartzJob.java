package com.taboola.cronyx.impl;

import com.taboola.cronyx.ArgumentDefinition;
import com.taboola.cronyx.ArgumentResolver;
import com.taboola.cronyx.Constants;
import com.taboola.cronyx.JobDefinition;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

public class DelegatingQuartzJob  implements Job {

    protected ListableBeanFactory listableBeanFactory;
    protected TypeConverter typeConverter;
    protected ArgumentResolver argumentResolver;

    public DelegatingQuartzJob(ListableBeanFactory listableBeanFactory,
                               TypeConverter typeConverter,
                               ArgumentResolver argumentResolver) {
        this.typeConverter = typeConverter;
        this.listableBeanFactory = listableBeanFactory;
        this.argumentResolver = argumentResolver;
    }


    /**
     * called by the quartz scheduler. this method does the actual invocation of the cronyx annotated class, using
     * previous introspection results store in the the JobDefinition and data passed along from the quartz context.
     * {@inheritDoc}
     * @param context
     * @throws JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            Object returnedValueObj;

            //get the job data from the quartz context
            JobDataMap dataMap = context.getMergedJobDataMap();

            //obtain the job bean name and a constructed JobDefinition
            String jobName = dataMap.getString(Constants.JOB_BEAN_NAME);
            JobDefinition jobDefinition = (JobDefinition) dataMap.get(Constants.JOB_DEFINITION);

            //obtain the actual job instance, and the method to invoke
            Object theJobInstance = listableBeanFactory.getBean(jobName);
            Method methodToInvoke = findJobMethod(jobDefinition);
            ReflectionUtils.makeAccessible(methodToInvoke);  //maybe make sure the method is public when introspecting

            //prepare the arguments for the method invocation.
            Object[] args = resolveAndConvertArguments(jobDefinition, context.getMergedJobDataMap(), context);

            //actually running the job.
            returnedValueObj = methodToInvoke.invoke(theJobInstance, args);

            //convert the return type if needed
            String convertedReturn = resolveReturnType(returnedValueObj);

            //save the result back to the context.
            context.setResult(convertedReturn);

        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new Error("This should never happen since we made sure this method exists in the first place", e);
        } catch (InvocationTargetException e) {
            throw new JobExecutionException("Job returned with exception", e);
        }
    }

    /**
     * using the data stored in the JobDefinition, finds the Method object to invoke.
     * @param jobDefinition of the job
     * @return a method object ready to invoke.
     * @throws NoSuchMethodException
     */
    protected Method findJobMethod(JobDefinition jobDefinition) throws NoSuchMethodException {
        Class<?> implementingClass = jobDefinition.getImplementingClass();
        return implementingClass.getMethod(
                jobDefinition.getMethodName(),
                Arrays.stream(jobDefinition.getArgs()).map(ArgumentDefinition::getType).toArray(Class[]::new)
        );
    }

    /**
     * converts the returned object to string (which quartz can handle)
     * @param returned
     * @return
     */
    protected String resolveReturnType(Object returned) {
        return typeConverter.convertIfNecessary(returned, String.class);
    }

    /**
     * using the data stored in the JobDefinition, and the current job data map, this method prepares the argument
     * array for invocation, extracting the appropriate data from the passed data map, and converting it if needed.
     * @param jobDefinition
     * @param dataMap
     * @return
     */
    protected Object[] resolveAndConvertArguments(JobDefinition jobDefinition, final Map<String, ?> dataMap, JobExecutionContext context){

        return Arrays.stream(jobDefinition.getArgs())
                     .map(ad -> argumentResolver.resolve(ad, dataMap, context))
                     .toArray();

    }
}
