package com.taboola.cronyx.builtin.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taboola.cronyx.annotations.Job;
import com.taboola.cronyx.annotations.JobMethod;

@Job(group = "builtin", name = "root")
public class RootJob {
    private static final Logger logger = LoggerFactory.getLogger(RootJob.class);

    /*
    This job does nothing at all. It is meant to serve as a job for a cron trigger that starts a trigger-chain
     */
    @JobMethod
    public void execute() {
        logger.info("root executed");
    }
}
