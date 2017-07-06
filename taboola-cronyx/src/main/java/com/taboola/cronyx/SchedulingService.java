package com.taboola.cronyx;

import java.util.List;

/**
 * The scheduling service is the main service used to interact with the scheduler.
 * This service is used to retrieve information about the currently configured triggers, and to update/remove triggers
 * from the scheduler.
 */
public interface SchedulingService {

    /**
     * @return a list of all of the triggers that are currently configured in the scheduler.
     */
    List<TriggerDefinition> getAllTriggers();

    /**
     * returns the triggers that belong to the provided trigger group.
     * @param group of which the trigger belongs to
     * @return a list of TriggerDefinitions
     */
    List<TriggerDefinition> getTriggersOfGroup(String group);

    /**
     * returns a list of all the available trigger group names.
     * @return a list of strings containing the group names.
     */
    List<String> getTriggerGroupNames();

    /**
     * returns a list of all the triggers that are configured for a specific job.
     * @param jobKey the job key for which to find triggers.
     * @return a list of TriggerDefinitions.
     */
    List<TriggerDefinition> getTriggersForJob(NameAndGroup jobKey);

    /**
     * retrieve the complete <code>com.taboola.scheduling.model.TriggerDefinition</code> by job key.
     * @param triggerKey key of the trigger to find.
     * @return the TriggerDefinition of the found job.
     */
    TriggerDefinition getTriggerByKey(NameAndGroup triggerKey);

    /**
     * This method is used to create a register a new trigger (schedule) or update a trigger registration (re-schedule)
     * Triggers are identified by their NameAndGroup triggerKey. (insert if the key does not exist / update existing key
     * if the trigger exists. )
     * @param updatedTrigger TriggerDefnition containing the updated trigger details
     */
    void saveOrUpdateTrigger(TriggerDefinition updatedTrigger);

    /**
     * Trigger an existing job immediately with the default misfire handling behavior.
     * @param jobKey The key of the job we wish to execute
     * @return The TriggerDefinition object used to schedule the job
     */
    TriggerDefinition triggerNow(NameAndGroup jobKey);

    /**
     * Trigger an existing job immediately with the specified job arguments and misfire handling instruction.
     * @param immediateTrigger The Immediate trigger definition object defining the trigger we wish to create
     */
    void triggerNow(Immediate immediateTrigger);

    /**
     * Trigger an existing job according to a cron expression.
     * @param cronTrigger The Cron trigger definition object defining the trigger we wish to create
     */
    void triggerCron(Cron cronTrigger);

    /**
     * This method is used to remove (un-schedule) a trigger registration.
     * @param triggerKey the key of the trigger to reemove.
     */
    void removeTrigger(NameAndGroup triggerKey);

    /**
     *
     * @return A list of all triggers that are currently being executed locally on this scheduler
     */
    List<TriggerDefinition> getLocallyExecutingTriggers();

    /**
     * Prevents any future execution of the specified trigger. If the specified trigger is currently executing it <b>will not</b> be killed
     * @param triggerKey The key of the trigger we wish to pause
     */
    void pauseTrigger(NameAndGroup triggerKey);

    /**
     * Prevents future execution of all triggers in the specified group. Currently executing triggers will <b>will not</b> be killed
     * New triggers in the same group will execute normally and will not start as paused
     * @param triggerGroup The group of triggers we wish to pause
     */
    void pauseTriggerGroup(String triggerGroup);

    /**
     * Pause any triggers for the specified job. Any new triggers for the job scheduled after a call to this method will execute normally
     * unless the job has been resumed. If a trigger is currently executing it <b>will not</b> be killed.
     * @param jobKey The key of the job for which we wish to pause all triggers
     */
    void pauseJob(NameAndGroup jobKey);

    /**
     * Pauses all triggers for jobs in the specified job group. Any new triggers for jobs in the group scheduled after a call to this method will execute normally
     * If a trigger is currently executing it <b>will not</b> be killed.
     * @param jobGroup The group whose triggers we wish to pause
     */
    void pauseJobGroup(String jobGroup);

    /**
     * Pauses all currently scheduled triggers. Any future triggers scheduled will execute normally
     * If a trigger is currently executing it <b>will not</b> be killed.
     */
    void pauseAll();

    /**
     * Resumes future firing of the specified trigger. Any missed firing during the period in which the trigger was paused
     * will be considered a misfire and its misfire policy will be applied
     * @param triggerKey The key of the trigger we wish to resume
     */
    void resumeTrigger(NameAndGroup triggerKey);

    /**
     * Resumes execution of all triggers in the specified trigger group. Any missed firing during the period in which the triggers were paused
     * will be considered a misfire and the trigger's misfire policy will be applied
     * @param triggerGroup The group whose triggers we want to resume
     */
    void resumeTriggerGroup(String triggerGroup);

    /**
     * Resumes future execution of triggers for the specified job. Any missed firing during the period in which the trigger was paused
     * will be considered a misfire and its misfire policy will be applied
     * @param jobKey The key of the job for which we wish to resume all triggers
     */
    void resumeJob(NameAndGroup jobKey);

    /**
     * Resumes all triggers in the specified job group. Any missed firing during the period in which the trigger was paused
     * will be considered a misfire and its misfire policy will be applied
     * @param jobGroup The group whose triggers we wish to resume
     */
    void resumeJobGroup(String jobGroup);

    /**
     * Resumes all currently paused triggers. Any missed firing during the period in which a trigger was paused
     * will be considered a misfire and its misfire policy will be applied
     */
    void resumeAll();

}