package com.taboola.cronyx.impl;

import static com.taboola.cronyx.Constants.CONTEXT_KEY;

import java.util.List;
import java.util.stream.Collectors;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;

import com.taboola.cronyx.CronyxExecutionContext;
import com.taboola.cronyx.HistorianEntry;
import com.taboola.cronyx.NameAndGroup;
import com.taboola.cronyx.TriggerDefinition;
import com.taboola.cronyx.exceptions.CronyxException;
import com.taboola.cronyx.impl.quartz.ondemand.OnDemandTrigger;

public class AfterProcessor implements Processor {

    private final Scheduler scheduler;
    private final AfterDAO afterDAO;
    private final HistorianDAO historianDAO;

    public AfterProcessor(Scheduler scheduler, AfterDAO afterDAO, HistorianDAO historianDAO) {
        this.scheduler = scheduler;
        this.afterDAO = afterDAO;
        this.historianDAO = historianDAO;
    }

    public void process(CronyxExecutionContext context) {
        List<NameAndGroup> finishedTriggersInContext = getSuccessfulTriggersInContext(context.getContextKey());
        TriggerDefinition firedTrigger = context.getFiredTrigger();
        List<NameAndGroup> candidateTriggersToFire = afterDAO.getAfterTriggersByKey(firedTrigger.getTriggerKey());
        candidateTriggersToFire
                .stream()
                .filter(nameAndGroupkey -> hasAllPreviousCompletedSuccessfully(nameAndGroupkey, finishedTriggersInContext))
                .map(nameAndGroupkey -> new TriggerKey(nameAndGroupkey.getName(), nameAndGroupkey.getGroup()))
                .forEach(triggerKey -> scheduleTrigger(triggerKey, context.getContextKey()));
    }

    private List<NameAndGroup> getSuccessfulTriggersInContext(String contextKey) {
        return historianDAO.readEntriesByContext(contextKey)
                .stream()
                .filter(e -> e.getRunStatus() == ExecutionStatus.COMPLETED_SUCCESSFULLY)
                .map(HistorianEntry::getTriggerKey)
                .collect(Collectors.toList());
    }

    private boolean hasAllPreviousCompletedSuccessfully(NameAndGroup triggerKey, List<NameAndGroup> completedSuccessfully) {
        List<NameAndGroup> previousTriggers = afterDAO.getPreviousTriggersByKey(triggerKey);
        return completedSuccessfully.containsAll(previousTriggers);
    }

    private void scheduleTrigger(TriggerKey triggerKey, String contextKey) {
        try {
            OnDemandTrigger trigger = (OnDemandTrigger) scheduler.getTrigger(triggerKey);
            trigger.getJobDataMap().put(CONTEXT_KEY, contextKey);
            trigger.setNextFireTimeToNow();
            scheduler.rescheduleJob(triggerKey, trigger);
        } catch (SchedulerException e) {
            throw new CronyxException("Could not fire trigger: " + triggerKey.toString(), e);
        }
    }
}