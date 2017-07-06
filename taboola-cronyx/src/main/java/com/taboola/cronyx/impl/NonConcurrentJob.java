package com.taboola.cronyx.impl;

import com.taboola.cronyx.ArgumentResolver;
import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.ListableBeanFactory;

@DisallowConcurrentExecution
public final class NonConcurrentJob extends DelegatingQuartzJob {

    public NonConcurrentJob(ListableBeanFactory listableBeanFactory, TypeConverter typeConverter, ArgumentResolver argumentResolver) {
        super(listableBeanFactory, typeConverter, argumentResolver);
    }
}
