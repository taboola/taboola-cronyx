package com.taboola.cronyx.impl;


import static com.taboola.cronyx.Constants.NEXT_FIRING_TIME;
import static com.taboola.cronyx.Constants.PREVIOUS_FIRING_TIME;
import static com.taboola.cronyx.TriggerDefinition.randomTriggerKey;
import static com.taboola.cronyx.TriggerDefinitionBuilder.immediate;
import static org.quartz.impl.matchers.GroupMatcher.groupEquals;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.quartz.CronTrigger;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.utils.Key;
import org.springframework.transaction.annotation.Transactional;

import com.taboola.cronyx.After;
import com.taboola.cronyx.Constants;
import com.taboola.cronyx.Cron;
import com.taboola.cronyx.Immediate;
import com.taboola.cronyx.JobDefinition;
import com.taboola.cronyx.JobsService;
import com.taboola.cronyx.NameAndGroup;
import com.taboola.cronyx.NameAndGroupOrderedPair;
import com.taboola.cronyx.SchedulingService;
import com.taboola.cronyx.TriggerDefinition;
import com.taboola.cronyx.exceptions.CronyxException;
import com.taboola.cronyx.impl.converter.cronyxtoquartz.CronyxToQuartzSelector;
import com.taboola.cronyx.impl.converter.quartztocronyx.QuartzToCronyxSelector;
import com.taboola.cronyx.impl.quartz.ondemand.OnDemandTrigger;

@Transactional(value="platformTransactionManager")
public class QuartzSchedulerServiceImpl implements SchedulingService {
    private JobsService jobsService;
    private Scheduler scheduler;
    private CronyxToQuartzSelector cronyxToQuartzSelector;
    private QuartzToCronyxSelector quartzToCronyxSelector;
    private AfterDAO afterDAO;
    private NameAndGroupGraphValidator graphValidator;

    public QuartzSchedulerServiceImpl(Scheduler scheduler, JobsService jobsService, CronyxToQuartzSelector cronyxToQuartzSelector, QuartzToCronyxSelector quartzToCronyxSelector, AfterDAO afterDAO, NameAndGroupGraphValidator graphValidator) {
        this.scheduler = scheduler;
        this.jobsService = jobsService;
        this.cronyxToQuartzSelector = cronyxToQuartzSelector;
        this.quartzToCronyxSelector = quartzToCronyxSelector;
        this.afterDAO = afterDAO;
        this.graphValidator = graphValidator;
    }

    @Override
    public List<TriggerDefinition> getAllTriggers() {
        return getTriggersForGroup(GroupMatcher.anyTriggerGroup());
    }

    @Override
    public List<TriggerDefinition> getTriggersOfGroup(String group) {
        return getTriggersForGroup(groupEquals(group));
    }

    @Override
    public List<String> getTriggerGroupNames() {
        try {
            return scheduler.getTriggerGroupNames();
        } catch (SchedulerException e) {
            throw new SchedulingException(e);
        }
    }

    @Override
    public List<TriggerDefinition> getTriggersForJob(NameAndGroup jobKey) {
        try {
            JobKey quartzJobKey = new JobKey(jobKey.getName(), jobKey.getGroup());
            return scheduler.getTriggersOfJob(quartzJobKey).stream()
                                 .map(quartzToCronyxSelector::convert)
                                 .map(this::enhanceTriggerDefinition)
                                 .collect(Collectors.toList());
        } catch (SchedulerException e) {
            throw new SchedulingException(e);
        }
    }

    @Override
    public TriggerDefinition getTriggerByKey(NameAndGroup triggerKey) {
        try {
            Trigger quartzTrigger = scheduler.getTrigger(new TriggerKey(triggerKey.getName(), triggerKey.getGroup()));
            if (quartzTrigger != null) {
                TriggerDefinition triggerDefinition =  quartzToCronyxSelector.convert(quartzTrigger);
                return enhanceTriggerDefinition(triggerDefinition);
            } else {
                return null;
            }
        } catch (SchedulerException e) {
            throw new SchedulingException(e);
        }
    }

    @Override
    public void saveOrUpdateTrigger(TriggerDefinition cronyxTrigger) {
        try {
            validateTrigger(cronyxTrigger);
            onStoreAfterTrigger(cronyxTrigger);
            Trigger newQuartzTrigger = cronyxToQuartzSelector.convert(cronyxTrigger);
            Trigger existingQuartzTrigger = scheduler.getTrigger(newQuartzTrigger.getKey());
            if (existingQuartzTrigger == null) {
                scheduler.scheduleJob(newQuartzTrigger);
            } else {
                updateExistingTrigger(newQuartzTrigger, existingQuartzTrigger);
            }
        } catch (SchedulerException e) {
            throw new SchedulingException(e);
        }
    }

    private void validateTrigger(TriggerDefinition cronyxTrigger) throws SchedulerException {
        Map<String, Object> jobDataMap = cronyxTrigger.getTriggerData();
        boolean isRetryable = isJobRetryable(cronyxTrigger);
        if (jobDataMap.containsKey(ErrorHandlingJob.NUMBER_OF_RETRIES_PARAM) && !isRetryable) {
            throw new SchedulerException("Job is not Retryable");
        }
    }

    private boolean isJobRetryable(TriggerDefinition cronyxTrigger) {
        JobDefinition jobDefinition = jobsService.getJobByKey(cronyxTrigger.getJobKey());
        return jobDefinition != null ? jobDefinition.isRetryable() : true;
    }

    private void onStoreAfterTrigger(TriggerDefinition cronyxTrigger) throws SchedulerException {
        if (cronyxTrigger instanceof After) {
            After afterTrigger = (After) cronyxTrigger;
            if (afterTrigger.getTriggerKey().getGroup() == null) {
                afterTrigger.getTriggerKey().setGroup(Key.DEFAULT_GROUP);
            }

            validateTriggersExist(afterTrigger.getPreviousTriggers());
            List<NameAndGroupOrderedPair> edges = afterDAO.getAllAncestorPairs(afterTrigger.getPreviousTriggers());
            afterTrigger.getPreviousTriggers().forEach(k -> edges.add(new NameAndGroupOrderedPair(k, afterTrigger.getTriggerKey())));
            graphValidator.validateGraph(edges);
            afterDAO.storeAfterTrigger(afterTrigger.getPreviousTriggers(), afterTrigger.getTriggerKey());
        }
    }

    private void validateTriggersExist(List<NameAndGroup> triggerKeys) throws SchedulerException {
        for(NameAndGroup key : triggerKeys) {
            if (!scheduler.checkExists(new TriggerKey(key.getName(), key.getGroup()))) {
                throw new CronyxException("No trigger found with given key: " + key);
            }
        }
    }

    private void updateExistingTrigger(Trigger quartzTrigger, Trigger existingQuartzTrigger) throws SchedulerException {
        if (!quartzTrigger.getJobKey().equals(existingQuartzTrigger.getJobKey())) {
            throw new SchedulingException(
                    String.format("the updated trigger's job has to be identical to that of the previous trigger. tried changing [%s] to [%s]",
                            existingQuartzTrigger.getJobKey().toString(), quartzTrigger.getJobKey().toString()));
        }

        TriggerBuilder triggerUpdater = existingQuartzTrigger.getTriggerBuilder();
        Trigger updatedTrigger = triggerUpdater.forJob(quartzTrigger.getJobKey())
                                               .withSchedule(quartzTrigger.getScheduleBuilder())
                                               .build();
        updatedTrigger.getJobDataMap().putAll(quartzTrigger.getJobDataMap());
        scheduler.rescheduleJob(updatedTrigger.getKey(), updatedTrigger);
    }

    @Override
    public TriggerDefinition triggerNow(NameAndGroup jobKey) {
        return triggerNow(jobKey, Collections.emptyMap(), Immediate.MisfireInstruction.DROP);
    }

    @Override
    public void triggerNow(Immediate immediateTrigger) {
        saveOrUpdateTrigger(immediateTrigger);
    }

    @Override
    public void triggerCron(Cron cronTrigger) {
        saveOrUpdateTrigger(cronTrigger);
    }

    @Override
    public void removeTrigger(NameAndGroup triggerKey) {
        try {
            List<NameAndGroup> afters = afterDAO.getAfterTriggersByKey(triggerKey);
            if(!afters.isEmpty()) {
                throw new CronyxException("Failed to delete the trigger because other trigger(s) depend on it: " + afters);
            }

            onRemoveAfterTrigger(triggerKey);
            TriggerKey quartzKey = new TriggerKey(triggerKey.getName(), triggerKey.getGroup());
            scheduler.unscheduleJob(quartzKey);
        } catch (SchedulerException e) {
            throw new SchedulingException(e);
        }
    }

    private void onRemoveAfterTrigger(NameAndGroup triggerKey) throws SchedulerException {
        if (scheduler.getTrigger(new TriggerKey(triggerKey.getName(), triggerKey.getGroup())) instanceof OnDemandTrigger) {
            List<NameAndGroupOrderedPair> edges = afterDAO.getAllAncestorPairs(Collections.singletonList(triggerKey));
            edges.removeIf(e -> e.getAfter().equals(triggerKey));
            if(edges.size() > 0) {
                graphValidator.validateGraph(edges);
            }

            afterDAO.deleteAfterTrigger(triggerKey);
        }
    }

    @Override
    public List<TriggerDefinition> getLocallyExecutingTriggers() {
        try {
            List<JobExecutionContext> jobs = scheduler.getCurrentlyExecutingJobs();
            return jobs.stream()
                       .map(JobExecutionContext::getTrigger)
                       .map(quartzToCronyxSelector::convert)
                       .map(this::enhanceTriggerDefinition)
                       .collect(Collectors.toList());
        } catch (SchedulerException e) {
            throw new SchedulingException(e);
        }
    }

    @Override
    public void pauseTrigger(NameAndGroup triggerKey) {
        TriggerKey quartzKey = new TriggerKey(triggerKey.getName(), triggerKey.getGroup());
        try {
            if(scheduler.getTrigger(quartzKey) instanceof CronTrigger) {
                scheduler.pauseTrigger(quartzKey);
            }
        } catch (SchedulerException e) {
            throw new SchedulingException(e);
        }
    }

    @Override
    public void pauseTriggerGroup(String triggerGroup) {
        getTriggersOfGroup(triggerGroup).stream()
                                        .map(TriggerDefinition::getTriggerKey)
                                        .forEach(this::pauseTrigger);
    }

    @Override
    public void pauseJob(NameAndGroup jobKey) {
        getTriggersForJob(jobKey).stream()
                                 .map(TriggerDefinition::getTriggerKey)
                                 .forEach(this::pauseTrigger);
    }

    @Override
    public void pauseJobGroup(String jobGroup) {
        jobsService.getJobsOfGroup(jobGroup).stream()
                                            .map(jd -> getTriggersForJob(jd.getKey()))
                                            .flatMap(Collection::stream)
                                            .map(TriggerDefinition::getTriggerKey)
                                            .forEach(this::pauseTrigger);
    }

    @Override
    public void pauseAll() {
        getAllTriggers().stream()
                        .map(TriggerDefinition::getTriggerKey)
                        .forEach(this::pauseTrigger);
    }

    @Override
    public void resumeTrigger(NameAndGroup triggerKey) {
        TriggerKey quartzKey = new TriggerKey(triggerKey.getName(), triggerKey.getGroup());
        try {
            scheduler.resumeTrigger(quartzKey);
        } catch (SchedulerException e) {
            throw new SchedulingException(e);
        }
    }

    @Override
    public void resumeTriggerGroup(String triggerGroup) {
        try {
            scheduler.resumeTriggers(groupEquals(triggerGroup));
        } catch (SchedulerException e) {
            throw new SchedulingException(e);
        }
    }

    @Override
    public void resumeJob(NameAndGroup jobKey) {
        JobKey quartzKey = new JobKey(jobKey.getName(), jobKey.getGroup());
        try {
            scheduler.resumeJob(quartzKey);
        } catch (SchedulerException e) {
            throw new SchedulingException(e);
        }
    }

    @Override
    public void resumeJobGroup(String jobGroup) {
        try {
            scheduler.resumeJobs(groupEquals(jobGroup));
        } catch (SchedulerException e) {
            throw new SchedulingException(e);
        }
    }

    @Override
    public void resumeAll() {
        try {
            scheduler.resumeAll();
        } catch (SchedulerException e) {
            throw new SchedulingException(e);
        }
    }

    private TriggerDefinition enhanceTriggerDefinition(TriggerDefinition triggerDefinition) {
        TriggerKey quartzTriggerKey = new TriggerKey(triggerDefinition.getTriggerKey().getName(), triggerDefinition.getTriggerKey().getGroup());
        Trigger quartzTrigger = getQuartzTriggerUnchecked(quartzTriggerKey);
        TriggerState quartzTriggerState = getQuartzTriggerStateUnchecked(quartzTriggerKey);
        TriggerStatus cronyxTriggerStatus = quartzTriggerStateToTriggerStatus(quartzTrigger, quartzTriggerState);

        addJobDefinition(triggerDefinition);
        addPreviousFiringTime(triggerDefinition, quartzTrigger);
        addNextFiringTime(triggerDefinition, quartzTrigger, cronyxTriggerStatus);
        addTriggerStatus(triggerDefinition, cronyxTriggerStatus);
        addPropertiesToAfterTrigger(triggerDefinition);

        return triggerDefinition;
    }

    // This is here because we need to access the db
    private void addPropertiesToAfterTrigger(TriggerDefinition triggerDefinition) {
        if (!(triggerDefinition instanceof After)){
            return;
        }
        After after = (After) triggerDefinition;
        List<NameAndGroup> prevTriggerKeys = afterDAO.getPreviousTriggersByKey(after.getTriggerKey());
        after.setPreviousTriggers(prevTriggerKeys);
        after.getTriggerData().put(Constants.PREVIOUS_TRIGGERS, prevTriggerKeys);
    }

    private void addTriggerStatus(TriggerDefinition triggerDefinition, TriggerStatus cronyxTriggerStatus) {
        if (cronyxTriggerStatus != null) {
            triggerDefinition.getTriggerData().put(Constants.TRIGGER_STATUS, cronyxTriggerStatus);
        }
    }

    private void addPreviousFiringTime(TriggerDefinition triggerDefinition, Trigger quartzTrigger) {
        triggerDefinition.getTriggerData().computeIfAbsent(
                PREVIOUS_FIRING_TIME,
                s -> quartzTrigger != null ?  quartzTrigger.getPreviousFireTime() : null
        );
    }

    private void addNextFiringTime(TriggerDefinition triggerDefinition, Trigger quartzTrigger, TriggerStatus cronyxTriggerStatus) {
        triggerDefinition.getTriggerData().computeIfAbsent(
                NEXT_FIRING_TIME,
                s -> quartzTrigger != null && cronyxTriggerStatus == TriggerStatus.ACTIVE ?  quartzTrigger.getNextFireTime() : null
        );
    }

    private void addJobDefinition(TriggerDefinition triggerDefinition) {
        triggerDefinition.getTriggerData().computeIfAbsent(
                Constants.JOB_DEFINITION,
                s -> jobsService.getJobByKey(triggerDefinition.getJobKey())
        );
    }

    /*
    Safe wrapper method. This method catches the exception normally thrown by the underlying method and returns an immutable empty list instead
     */
    private Trigger getTriggerSafe(TriggerKey triggerKey) {
        try {
            return scheduler.getTrigger(triggerKey);
        } catch (SchedulerException e) {
            throw new SchedulingException(e);
        }
    }

    private List<TriggerDefinition> getTriggersForGroup(GroupMatcher<TriggerKey> groupMatcher) {
        try {
            return scheduler.getTriggerKeys(groupMatcher).stream()
                            .map(this::getTriggerSafe)
                            .filter(trigger -> trigger != null)
                            .map(quartzToCronyxSelector::convert)
                            .map(this::enhanceTriggerDefinition)
                            .collect(Collectors.toList());
        } catch (SchedulerException e) {
            throw new SchedulingException(e);
        }
    }

    private static TriggerStatus quartzTriggerStateToTriggerStatus(Trigger quartzTrigger, TriggerState quartzTriggerState) {
        if (quartzTrigger != null && quartzTrigger.getJobDataMap().containsKey(Constants.JOB_EXCEPTION)) {
            return TriggerStatus.ERROR;
        }

        if (quartzTriggerState == null) {
            return null;
        }

        switch (quartzTriggerState) {
            case PAUSED:
                return TriggerStatus.PAUSED;
            case COMPLETE:
                return TriggerStatus.COMPLETE;
            case ERROR:
                return TriggerStatus.ERROR;
            case NORMAL:
            case BLOCKED:
                return TriggerStatus.ACTIVE;
            default:
                return null;
        }
    }

    private Trigger getQuartzTriggerUnchecked(TriggerKey quartzKey) {
        try {
            return scheduler.getTrigger(quartzKey);
        } catch (SchedulerException e) {
            throw new CronyxException(e);
        }
    }

    private TriggerState getQuartzTriggerStateUnchecked(TriggerKey quartzKey) {
        try {
            return scheduler.getTriggerState(quartzKey);
        } catch (SchedulerException e) {
            throw new CronyxException(e);
        }
    }

    private TriggerDefinition triggerNow(NameAndGroup jobKey,
                                         Map<String, Object> jobArguments,
                                         Immediate.MisfireInstruction misfire) {
        TriggerDefinition trigger =
                immediate().identifiedAs(randomTriggerKey())
                           .forJob(jobKey)
                           .withData(jobArguments)
                           .withMisfireInstruction(misfire)
                           .build();
        saveOrUpdateTrigger(trigger);
        return trigger;
    }
}