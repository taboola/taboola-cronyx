package com.taboola.scheduler.jobs;

import com.taboola.cronyx.annotations.Job;
import com.taboola.cronyx.annotations.JobMethod;

@Job(group = "example", name = "HelloWorld")
public class HelloWorld {

    @JobMethod
    public void exec() {
        System.out.println("Hello World");
    }
}
