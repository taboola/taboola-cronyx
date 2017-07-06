package com.taboola.cronyx.autoconfigure;

import com.codahale.metrics.MetricRegistry;
import com.taboola.cronyx.SchedulingService;
import com.taboola.cronyx.TriggerDefinition;
import com.taboola.cronyx.impl.MarkerGauge;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.Date;

import static com.taboola.cronyx.Constants.PREVIOUS_FIRING_TIME;
import static com.taboola.cronyx.util.MetricUtil.createLastExecutionMetricName;

public class MetricInitializerBeanPostProcessor implements BeanPostProcessor {

    private MetricRegistry metricRegistry;

    public MetricInitializerBeanPostProcessor(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof SchedulingService) {
            SchedulingService schedulingService = (SchedulingService) bean;
            for (TriggerDefinition trigger : schedulingService.getAllTriggers()) {
                String gaugeName = createLastExecutionMetricName(trigger);
                if (!metricRegistry.getGauges().containsKey(gaugeName)) {
                    Date lastFiringDate = (Date) trigger.getTriggerData().get(PREVIOUS_FIRING_TIME);
                    long lastFiringMillis = lastFiringDate == null ? 0 : lastFiringDate.getTime();
                    metricRegistry.register(gaugeName, new MarkerGauge(lastFiringMillis));
                }
            }
        }

        return bean;
    }
}
