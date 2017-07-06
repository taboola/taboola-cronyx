package com.taboola.cronyx.builtin.jobs;

import com.taboola.cronyx.annotations.Job;
import com.taboola.cronyx.annotations.JobMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Job(group = "builtin", name = "heartbeat")
public class HeartBeatJob {
    private static final Logger logger = LoggerFactory.getLogger(HeartBeatJob.class);

    /*
    This job does nothing at all. It is meant to serve as a "heart beat" whose execution alone means that the service is running
     */
    @JobMethod
    public void heartBeat() {
        logger.info("heartbeat");
    }
}
