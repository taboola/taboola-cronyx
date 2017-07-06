package com.taboola.cronyx;

import java.util.List;

/**
 * The jobs service exposes read only access to the job definitions that are available by the system.
 * Jobs can be either java or groovy jobs (annotated with the <code>@com.taboola.scheduling.api.Job</code> annotation),
 * or may be one of a number of built in jobs (e.g. send mail)
 *
 * This service exposes complete meta information on each of the jobs via
 * <code>com.taboola.scheduling.model.JobDefinition</code> object, which can be used by any client to create a
 * comprehensive user interaction. The meta-information is auto discovered by processing the annotations on classes
 * marked as <code>@Job</code>
 *
 */
public interface JobsService {


    /**
     * returns all of the jobs that were discovered by the system.
     * @return a list of JobDefinitions
     */
    List<JobDefinition> getAllJobs();

    /**
     * returns the jobs that belong to a specific job group.
     * @param group to which the jobs belong
     * @return a list of JobDefinitions
     */
    List<JobDefinition> getJobsOfGroup(String group);

    /**
     * returns a list of all the available job group names.
     * @return a list of strings containing the group names.
     */
    List<String> getJobGroupNames();


    /**
     * retrieve the complete <code>com.taboola.scheduling.model.JobDefinition</code> by job key.
     * @param jobKey key of the job to find.
     * @return the JobDefinition of the found job.
     */
    JobDefinition getJobByKey(NameAndGroup jobKey);

}
