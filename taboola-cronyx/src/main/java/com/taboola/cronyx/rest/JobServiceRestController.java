package com.taboola.cronyx.rest;

import java.util.List;

import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.actuate.endpoint.mvc.MvcEndpoint;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.taboola.cronyx.JobDefinition;
import com.taboola.cronyx.JobsService;
import com.taboola.cronyx.NameAndGroup;

@RestController
public class JobServiceRestController implements JobsService, MvcEndpoint {

    private JobsService underlyingScheduler;

    public JobServiceRestController(JobsService underlyingScheduler) {
        this.underlyingScheduler = underlyingScheduler;
    }

    @Override
    @RequestMapping(value = "/jobs/all", method = RequestMethod.GET, produces = "application/json")
    public List<JobDefinition> getAllJobs() {
        return underlyingScheduler.getAllJobs();
    }

    @Override
    @RequestMapping(value = "/jobs/group/{group}", method = RequestMethod.GET, produces = "application/json")
    public List<JobDefinition> getJobsOfGroup(@PathVariable("group") String group) {
        return underlyingScheduler.getJobsOfGroup(group);
    }

    @Override
    @RequestMapping(value = "/jobs/groups", method = RequestMethod.GET, produces = "application/json")
    public List<String> getJobGroupNames() {
        return underlyingScheduler.getJobGroupNames();
    }

    @Override
    @RequestMapping(value = "/jobs", method = RequestMethod.GET, produces = "application/json")
    public JobDefinition getJobByKey(NameAndGroup jobKey) {
        return underlyingScheduler.getJobByKey(jobKey);
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