package com.taboola.cronyx.impl;

import static com.taboola.cronyx.Constants.CONTEXT_KEY;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.TriggerKey;

import com.taboola.cronyx.HistorianEntry;
import com.taboola.cronyx.NameAndGroup;

public class HistorianJob implements Job {

    private Job job;
    private final Clock clock;
    private final String schedulerName;
    private final String schedulerInstanceId;
    private final HistorianDAO historianDAO;
    private final AfterDAO afterDAO;

    public HistorianJob(Job job, Clock clock, String schedulerName, String schedulerInstanceId, HistorianDAO historianDAO, AfterDAO afterDAO) {
        this.job = job;
        this.clock = clock;
        this.schedulerName = schedulerName;
        this.schedulerInstanceId = schedulerInstanceId;
        this.historianDAO = historianDAO;
        this.afterDAO = afterDAO;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        HistorianEntry entry = getInitialHistorianEntry(context);

        try {
            job.execute(context);
            entry.setOutput(context.getResult());
            entry.setRunStatus(ExecutionStatus.COMPLETED_SUCCESSFULLY);
        } catch (Throwable t) {
            entry.setException(t);
            entry.setRunStatus(ExecutionStatus.COMPLETED_WITH_EXCEPTION);
            throw t;
        } finally {
            entry.setEndTime(Instant.now(clock));
            historianDAO.writeEntry(entry);
        }
    }

    private HistorianEntry getInitialHistorianEntry(JobExecutionContext context) {
        String contextKey = context.get(CONTEXT_KEY).toString();
        TriggerKey triggerKey = context.getTrigger().getKey();
        NameAndGroup current = new NameAndGroup(triggerKey.getName(), triggerKey.getGroup());
        List<NameAndGroup> previouses = afterDAO.getPreviousTriggersByKey(current);
        List<String> prevTriggersFireKeys = historianDAO.readEntriesByContext(contextKey).stream().filter(e -> previouses.contains(e.getTriggerKey()))
                .map(HistorianEntry::getFireKey).collect(Collectors.toList());
        return new HistorianEntry(schedulerName, schedulerInstanceId, contextKey, context.getFireInstanceId(),
                current, prevTriggersFireKeys, Instant.now(clock), null, context.getTrigger().getJobDataMap(), null, ExecutionStatus.FIRED, null);
    }
}
