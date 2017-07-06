package com.taboola.cronyx.impl;

import java.time.Clock;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.codahale.metrics.MetricRegistry;

public class DelegatingQuartzJobFactory implements JobFactory, ApplicationContextAware {

    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        autowireCapableBeanFactory = applicationContext.getAutowireCapableBeanFactory();
    }

    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {

        JobDetail jobDetail = bundle.getJobDetail();
        MetricRegistry metricRegistry = autowireCapableBeanFactory.getBean(MetricRegistry.class);
        HistorianDAO historianDAO = autowireCapableBeanFactory.getBean(HistorianDAO.class);
        AfterDAO afterDAO = autowireCapableBeanFactory.getBean(AfterDAO.class);

        try {
            return new TriggerAwareLoggingJob(
                    new HistorianJob(
                            new TimedJob(
                                    new ErrorHandlingJob(
                                            (Job) autowireCapableBeanFactory.autowire(jobDetail.getJobClass(),
                                                    AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR, false)
                                    ),
                                    metricRegistry
                            ),
                            Clock.systemUTC(),
                            scheduler.getSchedulerName(),
                            scheduler.getSchedulerInstanceId(),
                            historianDAO,
                            afterDAO
                    )
            );
        } catch (Exception e) {
            throw new SchedulerException(
                    "Problem instantiating class '"
                            + jobDetail.getJobClass().getName() + "'", e);
        }
    }
}
