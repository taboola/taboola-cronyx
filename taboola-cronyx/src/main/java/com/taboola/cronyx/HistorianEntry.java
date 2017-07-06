package com.taboola.cronyx;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.List;

import org.quartz.JobDataMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.taboola.cronyx.impl.ExecutionStatus;

public class HistorianEntry {

    private final StringWriter sw = new StringWriter();
    private String schedulerName;
    private String schedulerInstanceId;
    private String contextKey;
    private String fireKey;
    private NameAndGroup triggerKey;
    private List<String> previousTriggerFireKeys;
    private Instant startTime;
    private Instant endTime;
    private JobDataMap input;
    private Object output;
    private ExecutionStatus runStatus;
    private String exception;

    public HistorianEntry(String schedulerName, String schedulerInstanceId, String contextKey, String fireKey, NameAndGroup triggerKey,
                          List<String> previousTriggerFireKeys, Instant startTime, Instant endTime, JobDataMap input,
                          Object output, ExecutionStatus runStatus, String exception) {
        this.schedulerName = schedulerName;
        this.schedulerInstanceId = schedulerInstanceId;
        this.contextKey = contextKey;
        this.fireKey = fireKey;
        this.triggerKey = triggerKey;
        this.previousTriggerFireKeys = previousTriggerFireKeys;
        this.startTime = startTime;
        this.endTime = endTime;
        this.input = input;
        this.output = output;
        this.runStatus = runStatus;
        this.exception = exception;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    public String getSchedulerInstanceId() {
        return schedulerInstanceId;
    }

    public void setSchedulerInstanceId(String schedulerInstanceId) {
        this.schedulerInstanceId = schedulerInstanceId;
    }

    public String getContextKey() {
        return contextKey;
    }

    public void setContextKey(String contextKey) {
        this.contextKey = contextKey;
    }

    public String getFireKey() {
        return fireKey;
    }

    public void setFireKey(String fireKey) {
        this.fireKey = fireKey;
    }

    public NameAndGroup getTriggerKey() {
        return triggerKey;
    }

    public void setTriggerKey(NameAndGroup triggerKey) {
        this.triggerKey = triggerKey;
    }

    public List<String> getPreviousTriggerFireKeys() {
        return previousTriggerFireKeys;
    }

    public void setPreviousTriggerFireKeys(List<String> previousTriggerFireKeys) {
        this.previousTriggerFireKeys = previousTriggerFireKeys;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public JobDataMap getInput() {
        return input;
    }

    public void setInput(JobDataMap input) {
        this.input = input;
    }

    public Object getOutput() {
        return output;
    }

    public void setOutput(Object output) {
        this.output = output;
    }

    public ExecutionStatus getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(ExecutionStatus runStatus) {
        this.runStatus = runStatus;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public void setException(Throwable exception) {
        exception.printStackTrace(new PrintWriter(sw));
        this.exception = sw.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HistorianEntry that = (HistorianEntry) o;

        if (schedulerName != null ? !schedulerName.equals(that.schedulerName) : that.schedulerName != null)
            return false;
        if (schedulerInstanceId != null ? !schedulerInstanceId.equals(that.schedulerInstanceId) : that.schedulerInstanceId != null) return false;
        if (contextKey != null ? !contextKey.equals(that.contextKey) : that.contextKey != null) return false;
        if (fireKey != null ? !fireKey.equals(that.fireKey) : that.fireKey != null) return false;
        if (triggerKey != null ? !triggerKey.equals(that.triggerKey) : that.triggerKey != null) return false;
        if (previousTriggerFireKeys != null ? !previousTriggerFireKeys.equals(that.previousTriggerFireKeys) : that.previousTriggerFireKeys != null)
            return false;
        if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null) return false;
        if (endTime != null ? !endTime.equals(that.endTime) : that.endTime != null) return false;
        if (input != null ? !input.equals(that.input) : that.input != null) return false;
        if (output != null ? !output.equals(that.output) : that.output != null) return false;
        if (runStatus != that.runStatus) return false;
        return exception != null ? exception.equals(that.exception) : that.exception == null;

    }

    @Override
    public int hashCode() {
        int result = schedulerName != null ? schedulerName.hashCode() : 0;
        result = 31 * result + (schedulerInstanceId != null ? schedulerInstanceId.hashCode() : 0);
        result = 31 * result + (contextKey != null ? contextKey.hashCode() : 0);
        result = 31 * result + (fireKey != null ? fireKey.hashCode() : 0);
        result = 31 * result + (triggerKey != null ? triggerKey.hashCode() : 0);
        result = 31 * result + (previousTriggerFireKeys != null ? previousTriggerFireKeys.hashCode() : 0);
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
        result = 31 * result + (input != null ? input.hashCode() : 0);
        result = 31 * result + (output != null ? output.hashCode() : 0);
        result = 31 * result + (runStatus != null ? runStatus.hashCode() : 0);
        result = 31 * result + (exception != null ? exception.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HistorianEntry{" +
                "schedulerName='" + schedulerName + '\'' +
                ", schedulerInstanceId='" + schedulerInstanceId + '\'' +
                ", contextKey='" + contextKey + '\'' +
                ", fireKey='" + fireKey + '\'' +
                ", triggerKey=" + triggerKey +
                ", previousTriggerFireKeys=" + previousTriggerFireKeys +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", input=" + input +
                ", output=" + output +
                ", runStatus=" + runStatus +
                ", exception=" + exception +
                '}';
    }
}
