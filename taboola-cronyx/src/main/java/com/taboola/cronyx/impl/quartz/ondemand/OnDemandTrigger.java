package com.taboola.cronyx.impl.quartz.ondemand;

import java.util.Date;

import org.quartz.Calendar;
import org.quartz.ScheduleBuilder;
import org.quartz.impl.triggers.AbstractTrigger;

public class OnDemandTrigger extends AbstractTrigger<OnDemandTrigger> {

    private static final Date ZERO_DATE = new Date(0);
    //translates to year 2255
    private static final Date MAX_DATE = new Date(9000000000000L);
    private static final int MISFIRE_INSTRUCTION_FIRE_NOW = 1;
    private Date previousFireTime;
    private Date nextFireTime;

    public OnDemandTrigger() {
        setNextFireTime(MAX_DATE);
    }

    @Override
    public void triggered(Calendar calendar) {
        setPreviousFireTime(nextFireTime);
        setNextFireTime(MAX_DATE);
    }

    @Override
    public Date computeFirstFireTime(Calendar calendar) {
        return ZERO_DATE;
    }

    @Override
    public boolean mayFireAgain() {
        return true;
    }

    @Override
    public Date getStartTime() {
        return new Date(0);
    }

    @Override
    public void setStartTime(Date startTime) {
    }

    @Override
    public void setEndTime(Date endTime) {
    }

    @Override
    public Date getEndTime() {
        return null;
    }

    @Override
    public Date getNextFireTime() {
        return nextFireTime;
    }

    @Override
    public Date getPreviousFireTime() {
        return previousFireTime;
    }

    @Override
    public Date getFireTimeAfter(Date afterTime) {
        return null;
    }

    @Override
    public Date getFinalFireTime() {
        return null;
    }

    @Override
    protected boolean validateMisfireInstruction(int candidateMisfireInstruction) {
        return (candidateMisfireInstruction == MISFIRE_INSTRUCTION_FIRE_NOW);
    }

    @Override
    public void updateAfterMisfire(Calendar cal) {
        setNextFireTime(new Date());
    }

    @Override
    public void updateWithNewCalendar(Calendar cal, long misfireThreshold) {

    }

    @Override
    public void setNextFireTime(Date nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    @Override
    public void setPreviousFireTime(Date previousFireTime) {
        this.previousFireTime = previousFireTime;
    }

    @Override
    public ScheduleBuilder<OnDemandTrigger> getScheduleBuilder() {
        return OnDemandScheduleBuilder.onDemandSchedule();
    }

    public void setNextFireTimeToNow() {
        setNextFireTime(new Date());
    }
}
