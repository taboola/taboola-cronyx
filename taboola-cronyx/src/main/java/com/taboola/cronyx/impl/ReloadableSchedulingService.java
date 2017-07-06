package com.taboola.cronyx.impl;

import com.taboola.cronyx.Cron;
import com.taboola.cronyx.Immediate;
import com.taboola.cronyx.NameAndGroup;
import com.taboola.cronyx.SchedulingService;
import com.taboola.cronyx.TriggerDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toConcurrentMap;
import static java.util.stream.Collectors.toList;

public class ReloadableSchedulingService implements SchedulingService {

    private SchedulingService schedulingService;
    private ConcurrentMap<NameAndGroup, TriggerDefinition> triggerRepository;

    public ReloadableSchedulingService(SchedulingService schedulingService, ConcurrentMap<NameAndGroup, TriggerDefinition> triggerRepository) {
        this.schedulingService = schedulingService;
        this.triggerRepository = triggerRepository;
    }

    @Override
    public List<TriggerDefinition> getAllTriggers() {
        return new ArrayList<>(triggerRepository.values());
    }

    @Override
    public List<TriggerDefinition> getTriggersOfGroup(String group) {
        return triggerRepository.values()
                                .stream()
                                .filter(td -> group.equals(td.getTriggerKey().getGroup()))
                                .collect(toList());
    }

    @Override
    public List<String> getTriggerGroupNames() {
        return triggerRepository.keySet()
                                .stream()
                                .map(NameAndGroup::getGroup)
                                .distinct()
                                .collect(toList());
    }

    @Override
    public List<TriggerDefinition> getTriggersForJob(NameAndGroup jobKey) {
        return triggerRepository.values()
                                .stream()
                                .filter(td -> jobKey.equals(td.getJobKey()))
                                .collect(toList());
    }

    @Override
    public TriggerDefinition getTriggerByKey(NameAndGroup triggerKey) {
        return triggerRepository.computeIfAbsent(triggerKey, schedulingService::getTriggerByKey);
    }

    @Override
    public void saveOrUpdateTrigger(TriggerDefinition updatedTrigger) {
        schedulingService.saveOrUpdateTrigger(updatedTrigger);
        refresh(updatedTrigger.getTriggerKey());
    }

    @Override
    public TriggerDefinition triggerNow(NameAndGroup jobKey) {
        TriggerDefinition td = schedulingService.triggerNow(jobKey);
        triggerRepository.put(td.getTriggerKey(), td);
        return td;
    }

    @Override
    public void triggerNow(Immediate immediateTrigger) {
        schedulingService.triggerNow(immediateTrigger);
        refresh(immediateTrigger.getTriggerKey());
    }

    @Override
    public void triggerCron(Cron cronTrigger) {
        schedulingService.triggerCron(cronTrigger);
        refresh(cronTrigger.getTriggerKey());
    }

    @Override
    public void removeTrigger(NameAndGroup triggerKey) {
        schedulingService.removeTrigger(triggerKey);
        triggerRepository.remove(triggerKey);
    }

    @Override
    public List<TriggerDefinition> getLocallyExecutingTriggers() {
        return schedulingService.getLocallyExecutingTriggers();
    }

    @Override
    public void pauseTrigger(NameAndGroup triggerKey) {
        schedulingService.pauseTrigger(triggerKey);
        refresh(triggerKey);
    }

    @Override
    public void pauseTriggerGroup(String triggerGroup) {
        List<NameAndGroup> triggersOfGroup =
                triggerRepository.values()
                                 .stream()
                                 .filter(td -> triggerGroup.equals(td.getTriggerKey().getGroup()))
                                 .map(TriggerDefinition::getTriggerKey)
                                 .collect(toList());
        schedulingService.pauseTriggerGroup(triggerGroup);
        refresh(triggersOfGroup);
    }

    @Override
    public void pauseJob(NameAndGroup jobKey) {
        List<NameAndGroup> triggersOfJob =
                triggerRepository.values()
                                 .stream()
                                 .filter(td -> jobKey.equals(td.getJobKey()))
                                 .map(TriggerDefinition::getTriggerKey)
                                 .collect(toList());
        schedulingService.pauseJob(jobKey);
        refresh(triggersOfJob);
    }

    @Override
    public void pauseJobGroup(String jobGroup) {
        List<NameAndGroup> triggersOfJobGroup =
                triggerRepository.values()
                                 .stream()
                                 .filter(td -> jobGroup.equals(td.getJobKey().getGroup()))
                                 .map(TriggerDefinition::getTriggerKey)
                                 .collect(toList());
        schedulingService.pauseJobGroup(jobGroup);
        refresh(triggersOfJobGroup);
    }

    @Override
    public void pauseAll() {
        schedulingService.pauseAll();
        refresh();
    }

    @Override
    public void resumeTrigger(NameAndGroup triggerKey) {
        schedulingService.resumeTrigger(triggerKey);
        refresh(triggerKey);
    }

    @Override
    public void resumeTriggerGroup(String triggerGroup) {
        List<NameAndGroup> triggersOfGroup =
                triggerRepository.values()
                                 .stream()
                                 .filter(td -> triggerGroup.equals(td.getTriggerKey().getGroup()))
                                 .map(TriggerDefinition::getTriggerKey)
                                 .collect(toList());
        schedulingService.resumeTriggerGroup(triggerGroup);
        refresh(triggersOfGroup);
    }

    @Override
    public void resumeJob(NameAndGroup jobKey) {
        List<NameAndGroup> triggersOfJob =
                triggerRepository.values()
                                 .stream()
                                 .filter(td -> jobKey.equals(td.getJobKey()))
                                 .map(TriggerDefinition::getTriggerKey)
                                 .collect(toList());
        schedulingService.resumeJob(jobKey);
        refresh(triggersOfJob);
    }

    @Override
    public void resumeJobGroup(String jobGroup) {
        List<NameAndGroup> triggersOfJobGroup =
                triggerRepository.values()
                                 .stream()
                                 .filter(td -> jobGroup.equals(td.getJobKey().getGroup()))
                                 .map(TriggerDefinition::getTriggerKey)
                                 .collect(toList());
        schedulingService.resumeJobGroup(jobGroup);
        refresh(triggersOfJobGroup);
    }

    @Override
    public void resumeAll() {
        schedulingService.resumeAll();
        refresh();
    }

    public void reload() {
        triggerRepository = schedulingService.getAllTriggers()
                                             .stream()
                                             .collect(toConcurrentMap(TriggerDefinition::getTriggerKey, identity()));
    }

    private void refresh(NameAndGroup triggerKey) {
        triggerRepository.put(triggerKey, schedulingService.getTriggerByKey(triggerKey));
    }

    private void refresh(Collection<NameAndGroup> triggerKeys) {
        triggerKeys.forEach(this::refresh);
    }

    private void refresh() {
        refresh(triggerRepository.keySet());
    }
}
