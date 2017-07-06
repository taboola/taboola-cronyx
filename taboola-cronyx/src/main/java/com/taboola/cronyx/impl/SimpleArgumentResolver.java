package com.taboola.cronyx.impl;

import com.taboola.cronyx.ArgumentDefinition;
import com.taboola.cronyx.ArgumentResolutionException;
import com.taboola.cronyx.ArgumentResolver;
import com.taboola.cronyx.annotations.ScheduledTime;
import com.taboola.cronyx.annotations.SpringQualifier;
import com.taboola.cronyx.annotations.UserInput;
import org.quartz.JobExecutionContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

public class SimpleArgumentResolver implements ArgumentResolver {

    private BeanFactory listableBeanFactory;
    private TypeConverter typeConverter;

    public SimpleArgumentResolver(BeanFactory listableBeanFactory, TypeConverter typeConverter) {
        this.listableBeanFactory = listableBeanFactory;
        this.typeConverter = typeConverter;
    }

    @Override
    public Object resolve(ArgumentDefinition definition, Map<String, ?> dataMap, JobExecutionContext context) throws ArgumentResolutionException {
        Annotation argumentDescriptor = definition.getDescriptor();
        if (argumentDescriptor instanceof UserInput) {
            UserInput input = (UserInput) argumentDescriptor;
            String stringValue = getStringFromMap(dataMap, input.name(), input.defaultValue());
            return typeConverter.convertIfNecessary(stringValue, definition.getType());
        } else if (argumentDescriptor instanceof ScheduledTime) {
            return LocalDateTime.ofInstant(context.getScheduledFireTime().toInstant(), ZoneId.systemDefault());
        } else if (argumentDescriptor instanceof SpringQualifier) {
            SpringQualifier input = (SpringQualifier) argumentDescriptor;
            String beanName = getStringFromMap(dataMap, input.beanSource(), input.defaultBeanName());
            try {
                return listableBeanFactory.getBean(beanName);
            } catch (BeansException e) {
                throw new ArgumentResolutionException(String.format("failed to instantiate spring bean [%s]", beanName), e);
            }
        } else {
            return null;
        }
    }

    /**
     * extract a data value from a map of either <String,Object> or <String,List<Object>>
     */
    private static String getStringFromMap(Map<String, ?> dataMap, String key, String defaultValue) {
        Object found = dataMap.get(key);
        if (found instanceof List && ((List) found).get(0) instanceof String) {
            return (String) ((List) found).get(0);
        } else if (found instanceof String) {
            return (String) found;
        } else {
            return defaultValue;
        }
    }
}
