package com.taboola.scheduler.jobs;


import com.taboola.cronyx.annotations.Job;
import com.taboola.cronyx.annotations.JobMethod;

import java.util.Date;

@Job(value = "the-other-job")
public class ExampleJob2 {

    @JobMethod
    public String runExample(
    ){

        try {
            System.out.println("the-other-job before sleep " + new Date().toString());
            Thread.sleep(2000);
            System.out.println("the-other-job after sleep " + new Date().toString());
        } catch (InterruptedException e) {
            throw new RuntimeException();
        }
        return "job2 " + new Date().toGMTString();

    }
}
