package com.taboola.scheduler.jobs;


import com.taboola.cronyx.annotations.Job;
import com.taboola.cronyx.annotations.JobMethod;
import com.taboola.cronyx.annotations.UserInput;

import java.util.Date;

@Job(value = "an-example-job")
public class ExampleJob {

//    @Resource(name = "dbDataSource")
//    DataSource dataSource;

    @JobMethod
    public String runExample(
            @UserInput(name = "testparam") String param,
            @UserInput(name = "testparam2", defaultValue = "12345") String param2
    ){

        try {
            System.out.println("before sleep " + new Date().toString());
            Thread.sleep(5000);
            System.out.println("after sleep " + new Date().toString());
        } catch (InterruptedException e) {
            throw new RuntimeException();
        }
        return "OK";//new ExampleStatus(param + param2 + " " + new Date().toGMTString());

    }


}
