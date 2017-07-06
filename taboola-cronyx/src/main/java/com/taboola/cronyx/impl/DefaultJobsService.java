package com.taboola.cronyx.impl;

import com.taboola.cronyx.Constants;
import com.taboola.cronyx.JobDefinition;
import com.taboola.cronyx.JobsService;
import com.taboola.cronyx.NameAndGroup;
import com.taboola.cronyx.exceptions.CronyxException;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultJobsService implements JobsService {

    private Scheduler scheduler;

    public DefaultJobsService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public List<JobDefinition> getAllJobs() {
        return getJobsFor(GroupMatcher.anyJobGroup()).collect(Collectors.toList());
    }

    @Override
    public List<JobDefinition> getJobsOfGroup(String group) {
        return getJobsFor(GroupMatcher.jobGroupEquals(group)).collect(Collectors.toList());
    }

    @Override
    public List<String> getJobGroupNames() {
        return getJobsFor(GroupMatcher.anyJobGroup()).map(jd->jd.getKey().getGroup()).distinct().collect(Collectors.toList());
    }

    @Override
    public JobDefinition getJobByKey(NameAndGroup nameAndGroup) {
        try {
            JobDetail jobDetail = scheduler.getJobDetail(JobKey.jobKey(nameAndGroup.getName(), nameAndGroup.getGroup()));
            if (jobDetail != null) {
                return (JobDefinition) jobDetail
                        .getJobDataMap()
                        .get(Constants.JOB_DEFINITION);
            } else {
                return null;
            }
        } catch (SchedulerException e) {
            throw new CronyxException(e);
        }
    }

    private Stream<JobDefinition> getJobsFor(GroupMatcher<JobKey> matcher){
        try {
            return scheduler.getJobKeys(matcher)
                            .stream()
                            .map(jk -> {
                                try {
                                    return scheduler.getJobDetail(jk);
                                } catch (SchedulerException e) {
                                    throw new CronyxException("failed to retrieve job details for " + jk, e);
                                }
                            })
                            .map((JobDetail jd) -> (JobDefinition) jd.getJobDataMap().get(Constants.JOB_DEFINITION));
        } catch (SchedulerException e) {
            throw new CronyxException(e);
        }
    }

}
