package com.taboola.cronyx.impl;

import com.taboola.cronyx.Constants;
import com.taboola.cronyx.JobDefinition;
import com.taboola.cronyx.annotations.Job;
import com.taboola.cronyx.util.QuartzJobClassSelector;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public class QuartzJobRegistrarBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(QuartzJobRegistrarBeanFactoryPostProcessor.class);

    private JobIntrospecter jobIntrospecter;
    private Scheduler scheduler;
    private QuartzJobClassSelector jobClassSelector;

    public QuartzJobRegistrarBeanFactoryPostProcessor(JobIntrospecter jobIntrospecter, Scheduler scheduler, QuartzJobClassSelector selector) {
        this.jobIntrospecter = jobIntrospecter;
        this.scheduler = scheduler;
        this.jobClassSelector = selector;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        String[] beanNames = beanFactory.getBeanNamesForAnnotation(Job.class);

        for(String name : beanNames){
            JobDetail jobDetail = buildJobForBeanFactory(beanFactory, name);

            if (jobDetail == null){
                logger.warn("could not load JobDetail for {}", name);
                continue;
            }

            try {
                scheduler.addJob(jobDetail, true);
            } catch (SchedulerException e) {
                throw new BeanInitializationException("SchedulerException when adding job to scheduler", e);
            }
        }
    }

    protected JobDetail buildJobForBeanFactory(ConfigurableListableBeanFactory beanFactory, String jobBeanName) {
        try {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(jobBeanName);
            Class beanClass = Class.forName(beanDefinition.getBeanClassName());

            JobDefinition jobDefinition = jobIntrospecter.getJobDefinitionForInstance(beanClass);

            JobDataMap jdm = new JobDataMap();

            jdm.put(Constants.JOB_CRONYX_MARKER, true);
            jdm.put(Constants.JOB_BEAN_NAME, jobBeanName);
            jdm.put(Constants.JOB_DEFINITION, jobDefinition);

            return JobBuilder.newJob()
                    .withIdentity(jobDefinition.getKey().getName(), jobDefinition.getKey().getGroup())
                    .withDescription(jobDefinition.getDescription())
                    .ofType(jobClassSelector.select(jobDefinition))
                    .usingJobData(jdm)
                    .storeDurably()
                    .requestRecovery(jobDefinition.isRecoverable())
                    .build();

        }catch (Exception e) {
            logger.error("problem creating job detail, logging and going on. ", e);
            return null;
        }
    }


}
