package com.taboola.cronyx.impl.quartz.ondemand

import com.taboola.cronyx.After
import com.taboola.cronyx.Constants
import com.taboola.cronyx.EnableCronyx
import com.taboola.cronyx.annotations.Job
import com.taboola.cronyx.annotations.JobMethod
import com.taboola.cronyx.annotations.UserInput
import com.taboola.cronyx.testing.TransientCronyxConfiguration
import org.quartz.JobDataMap
import org.quartz.JobKey
import org.quartz.Scheduler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Configuration
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

import static com.taboola.cronyx.impl.quartz.ondemand.OnDemandScheduleBuilder.onDemandSchedule
import static org.quartz.TriggerBuilder.newTrigger

@Unroll
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.NONE, classes = [TransientCronyxConfiguration.class, QuartzOnDemandTriggerIntegrationTest.class])
@Configuration
class QuartzOnDemandTriggerIntegrationTest extends Specification {

    static final Date MAX_DATE = new Date(9000000000000L);
    static final long TIMEOUT = 3000;
    static final String numOfLocksStr = "1";

    @Autowired
    Scheduler mainScheduler
    def exeuctorService = Executors.newSingleThreadExecutor()

    def setup() {
        def job = mainScheduler.getJobDetail(new JobKey("jobName", "jobGroup"));
        mainScheduler.clear()
        mainScheduler.addJob(job, true)
    }

    def "schedule a dummy job with on demand trigger"() {
        given: "on demand trigger for a dummy job"
        def trigger = onDemandTrigger();
        def prevFireTime = trigger.getPreviousFireTime()

        when: "scheduling a dummy job"
        mainScheduler.scheduleJob(trigger)
        sleep(TIMEOUT)

        then: "the task does not start, prev fire time does not change, next fire time is set to max date"
        trigger.getPreviousFireTime() == prevFireTime
        trigger.getNextFireTime() == MAX_DATE
    }

    def "triggering a dummy job with on demand trigger"() {
        given: "on demand trigger for a dummy job"

        def triggerToFire = onDemandTrigger()

        when: "triggering on demand trigger with a dummy job and waiting for it to end"
        triggerToFire.setNextFireTimeToNow()
        mainScheduler.scheduleJob(triggerToFire)
        sleep(TIMEOUT)

        then: "the task ends in time and changes prev fire time, next fire time remains the same"
        mainScheduler.getTrigger(triggerToFire.getKey()).getPreviousFireTime() != triggerToFire.getPreviousFireTime()
        mainScheduler.getTrigger(triggerToFire.getKey()).getNextFireTime() == MAX_DATE
    }

    def "scheduling a dummy job with on demand trigger, then triggering it"() {
        given: "on demand trigger for a dummy job"
        def trigger = onDemandTrigger();

        when: "triggering on demand trigger with a dummy job and waiting for it to end"
        mainScheduler.scheduleJob(trigger)
        def triggerToFire = (OnDemandTrigger) mainScheduler.getTrigger(trigger.getKey())
        triggerToFire.setNextFireTimeToNow()
        mainScheduler.rescheduleJob(trigger.getKey(), triggerToFire)
        sleep(TIMEOUT)

        then: "the task ends in time and changes prev fire time. after task is triggered next fire time will be max date"
        mainScheduler.getTrigger(trigger.getKey()).getPreviousFireTime() != triggerToFire.getPreviousFireTime()
        mainScheduler.getTrigger(trigger.getKey()).getNextFireTime() == MAX_DATE
    }

    def onDemandTrigger() {
        def map = new HashMap()
        map.put("locks", numOfLocksStr)
        map.put(Constants.CRONYX_TYPE, After.class)
        def jobData = new JobDataMap(map)

        return newTrigger().withIdentity("triggerName", "triggerGroup")
                .forJob("jobName", "jobGroup")
                .withDescription("test description")
                .usingJobData(jobData)
                .withSchedule(onDemandSchedule())
                .build()
    }

    @Job(name = "jobName", group = "jobGroup")
    public static class DummyJob {
        static Semaphore lock = new Semaphore(0)
        static int resource = 0

        @JobMethod
        public void jobMethod(@UserInput(name = "locks") int locks) {
            resource = locks
            lock.release()
        }
    }

    def awaitTermination(int timeout) {
        exeuctorService.submit({ DummyJob.lock.acquire() }).get(timeout, TimeUnit.MILLISECONDS)
    }
}
