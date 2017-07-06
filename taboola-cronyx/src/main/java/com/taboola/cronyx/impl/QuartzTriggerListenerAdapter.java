package com.taboola.cronyx.impl;

import static com.taboola.cronyx.Constants.CONTEXT_KEY;

import org.apache.commons.lang3.tuple.Pair;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taboola.cronyx.Constants;
import com.taboola.cronyx.CronyxExecutionContext;
import com.taboola.cronyx.FiringListener;
import com.taboola.cronyx.Registry;
import com.taboola.cronyx.impl.converter.JobExecutionContextToFiringContext;

public class QuartzTriggerListenerAdapter implements TriggerListener {
    private static final Logger logger = LoggerFactory.getLogger(QuartzTriggerListenerAdapter.class);
    private static final String ADAPTER_NAME = "_TRIGGER_LISTENER_ADAPTER";

    private Registry<String, Pair<ListenerMatcher, FiringListener>> listenerRegistry;
    private Registry<String, CronyxExecutionContext> contextRegistry;
    private JobExecutionContextToFiringContext jobExecutionContextToFiringContext;

    public QuartzTriggerListenerAdapter(Registry<String, Pair<ListenerMatcher, FiringListener>> listenerRegistry,
                                        Registry<String, CronyxExecutionContext> contextRegistry, JobExecutionContextToFiringContext jobExecutionContextToFiringContext) {
        this.listenerRegistry = listenerRegistry;
        this.contextRegistry = contextRegistry;
        this.jobExecutionContextToFiringContext = jobExecutionContextToFiringContext;
    }

    @Override
    public String getName() {
        return ADAPTER_NAME;
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        context.put(CONTEXT_KEY, context.getMergedJobDataMap().get(CONTEXT_KEY) == null ? context.getFireInstanceId() : context.getMergedJobDataMap().get(CONTEXT_KEY));
        CronyxExecutionContext cronyxContext = contextRegistry.getOrRegister(context.getFireInstanceId(), () -> jobExecutionContextToFiringContext.convert(context));
        onEvent(cronyxContext);
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {

    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context, Trigger.CompletedExecutionInstruction triggerInstructionCode) {
        CronyxExecutionContext cronyxContext = contextRegistry.get(context.getFireInstanceId());
        if (cronyxContext == null) {
            logger.error("there is no active context registered with the name [%s]", context.getFireInstanceId());
            throw new Error("this should never happen as a triggetFired event should always be invoked before triggerComplete");
        }

        updateCronyxContext(context, cronyxContext);
        onEvent(cronyxContext);
        contextRegistry.unregister(context.getFireInstanceId());
    }

    private void updateCronyxContext(JobExecutionContext context, CronyxExecutionContext cronyxContext) {
        cronyxContext.setExecutionResult(context.getResult());
        cronyxContext.setExecutionException((Throwable) context.get(Constants.JOB_EXCEPTION));
        cronyxContext.setCurrentStatus(
                cronyxContext.getExecutionException() == null ?
                ExecutionStatus.COMPLETED_SUCCESSFULLY :
                ExecutionStatus.COMPLETED_WITH_EXCEPTION);
    }

    private void onEvent(CronyxExecutionContext context) {
        listenerRegistry.getAll()
                .values()
                .stream()
                .filter(pair -> pair.getLeft().test(context))
                .forEach(pair -> {
                    try {
                        pair.getRight().onEvent(context);
                    } catch (Throwable t) {
                        logger.error("failed to execute firing listener for trigger: " + context.getFiredTrigger().getTriggerKey(), t);
                    }
                });
    }
}