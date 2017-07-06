package com.taboola.cronyx.rest;

import com.taboola.cronyx.Cron;
import com.taboola.cronyx.Immediate;
import com.taboola.cronyx.NameAndGroup;
import com.taboola.cronyx.SchedulingService;
import com.taboola.cronyx.TriggerDefinition;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.actuate.endpoint.mvc.MvcEndpoint;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SchedulingServiceRestController implements SchedulingService, MvcEndpoint {

    private SchedulingService underlyingScheduler;

    public SchedulingServiceRestController(SchedulingService underlyingScheduler) {
        this.underlyingScheduler = underlyingScheduler;
    }

    @Override
    @RequestMapping(value = "/triggers/all", method = RequestMethod.GET, produces = "application/json")
    public List<TriggerDefinition> getAllTriggers() {
        return underlyingScheduler.getAllTriggers();
    }

    @Override
    @RequestMapping(value = "/triggers/group/{group}", method = RequestMethod.GET, produces = "application/json")
    public List<TriggerDefinition> getTriggersOfGroup(@PathVariable("group") String group) {
        return underlyingScheduler.getTriggersOfGroup(group);
    }

    @Override
    @RequestMapping(value = "/triggers/groups", method = RequestMethod.GET, produces = "application/json")
    public List<String> getTriggerGroupNames() {
        return underlyingScheduler.getTriggerGroupNames();
    }

    @Override
    @RequestMapping(value = "/triggers/job", method = RequestMethod.GET, produces = "application/json")
    public List<TriggerDefinition> getTriggersForJob(NameAndGroup jobKey) {
        return underlyingScheduler.getTriggersForJob(jobKey);
    }

    @Override
    @RequestMapping(value = "/triggers", method = RequestMethod.GET, produces = "application/json")
    public TriggerDefinition getTriggerByKey(NameAndGroup triggerKey) {
        return underlyingScheduler.getTriggerByKey(triggerKey);
    }

    @Override
    @RequestMapping(value = "/triggers/new")
    public void saveOrUpdateTrigger(@RequestBody TriggerDefinition updatedTrigger) {
        underlyingScheduler.saveOrUpdateTrigger(updatedTrigger);
    }

    @Override
    @RequestMapping(value = "/triggers/start", method = RequestMethod.POST, produces = "application/json")
    public TriggerDefinition triggerNow(NameAndGroup jobKey) {
        return underlyingScheduler.triggerNow(jobKey);
    }

    @Override
    @RequestMapping(value = "/triggers/new/immediate")
    public void triggerNow(@RequestBody Immediate immediateTrigger) {
        underlyingScheduler.saveOrUpdateTrigger(immediateTrigger);
    }

    @Override
    @RequestMapping(value = "/triggers/new/cron")
    public void triggerCron(@RequestBody Cron cronTrigger) {
        underlyingScheduler.saveOrUpdateTrigger(cronTrigger);
    }

    @Override
    @RequestMapping(value = "/triggers/delete", method = RequestMethod.POST)
    public void removeTrigger(NameAndGroup triggerKey) {
        underlyingScheduler.removeTrigger(triggerKey);
    }

    @Override
    @RequestMapping(value = "/triggers/local", method = RequestMethod.GET, produces = "application/json")
    public List<TriggerDefinition> getLocallyExecutingTriggers() {
        return underlyingScheduler.getLocallyExecutingTriggers();
    }

    @Override
    @RequestMapping(value = "/triggers/pause", method = RequestMethod.POST)
    public void pauseTrigger(NameAndGroup triggerKey) {
        underlyingScheduler.pauseTrigger(triggerKey);
    }

    @Override
    @RequestMapping(value = "/triggers/pause/tgroup/{triggerGroup}", method = RequestMethod.POST)
    public void pauseTriggerGroup(@PathVariable("triggerGroup") String triggerGroup) {
        underlyingScheduler.pauseTriggerGroup(triggerGroup);
    }

    @Override
    @RequestMapping(value = "/triggers/pause/job", method = RequestMethod.POST)
    public void pauseJob(NameAndGroup jobKey) {
        underlyingScheduler.pauseJob(jobKey);
    }

    @Override
    @RequestMapping(value = "/triggers/pause/jgroup/{jobGroup}", method = RequestMethod.POST)
    public void pauseJobGroup(@PathVariable("jobGroup") String jobGroup) {
        underlyingScheduler.pauseJobGroup(jobGroup);
    }

    @Override
    @RequestMapping(value = "/triggers/pause/all", method = RequestMethod.POST)
    public void pauseAll() {
        underlyingScheduler.pauseAll();
    }

    @Override
    @RequestMapping(value = "/triggers/resume", method = RequestMethod.POST)
    public void resumeTrigger(NameAndGroup triggerKey) {
        underlyingScheduler.resumeTrigger(triggerKey);
    }

    @Override
    @RequestMapping(value = "/triggers/resume/tgroup/{triggerGroup}", method = RequestMethod.POST)
    public void resumeTriggerGroup(@PathVariable("triggerGroup") String triggerGroup) {
        underlyingScheduler.resumeTriggerGroup(triggerGroup);
    }

    @Override
    @RequestMapping(value = "/triggers/resume/job", method = RequestMethod.POST)
    public void resumeJob(NameAndGroup jobKey) {
        underlyingScheduler.resumeJob(jobKey);
    }

    @Override
    @RequestMapping(value = "/triggers/resume/jgroup/{jobGroup}", method = RequestMethod.POST)
    public void resumeJobGroup(@PathVariable("jobGroup") String jobGroup) {
        underlyingScheduler.resumeJobGroup(jobGroup);
    }

    @Override
    @RequestMapping(value = "/triggers/resume/all", method = RequestMethod.POST)
    public void resumeAll() {
        underlyingScheduler.resumeAll();
    }

    @Override
    public String getPath() {
        return "/scheduling";
    }

    @Override
    public boolean isSensitive() {
        return false;
    }

    @Override
    public Class<? extends Endpoint> getEndpointType() {
        return null;
    }
}