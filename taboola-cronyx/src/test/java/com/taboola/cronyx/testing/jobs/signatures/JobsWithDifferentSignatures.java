package com.taboola.cronyx.testing.jobs.signatures;

import com.taboola.cronyx.annotations.Job;
import com.taboola.cronyx.annotations.JobMethod;
import com.taboola.cronyx.annotations.UserInput;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobsWithDifferentSignatures {
    ///////////////////////////////
    // Simple Jobs

    @Job
    public static class NoArgJob {
        @JobMethod
        public String theJob(){
            throw new UnsupportedOperationException();
        }
    }

    @Job
    public static class OneArgJob {
        @JobMethod
        public String theJob(@UserInput(name="simpleArg") String simpleArg){
            throw new UnsupportedOperationException();
        }
    }


    ///////////////////////////////
    // Naming
    @Job(value = "different-name-from-annotation")
    public static class NamedJob {
        @JobMethod
        public String theJob(){
            throw new UnsupportedOperationException();
        }
    }

//@TODO: look into re-enabling this.
//    @Job(name = "different-name-from-annotation")
//    public static class NamedNotValueJob {
//        @JobMethod
//        public String theJob(){
//            throw new UnsupportedOperationException();
//        }
//    }

    @Job(group = "not-the-default")
    public static class JobInGroup {
        @JobMethod
        public String theJob(){
            throw new UnsupportedOperationException();
        }
    }

    @Job(group = "not-the-default", value = "also-not-default")
    public static class NamedJobInGroup {
        @JobMethod
        public String theJob(){
            throw new UnsupportedOperationException();
        }
    }

    @Job
    public static class NamedArgJob {
        @JobMethod
        public String theJob(@UserInput(name = "different-arg-name") String originalArgName){
            throw new UnsupportedOperationException();
        }
    }

    @Job(description = "Job that does nothing")
    public static class DescriptiveJob {
        @JobMethod
        public String theJob(@UserInput(name="theArg", description = "An argument that is simply ignored.") String theArg){
            throw new UnsupportedOperationException();
        }
    }


    ///////////////////////////////
    // Arguments

    @Job
    public static class TwoArgJob {
        @JobMethod
        public String theJob(@UserInput(name="firstArg") String firstArg,
                             @UserInput(name="secondArg") String secondArg){
            throw new UnsupportedOperationException();
        }
    }

    @Job
    public static class DifferentArgTypeJob {
        @JobMethod
        public String theJob(@UserInput(name="aString") String aString,
                             @UserInput(name="anInteger") Integer anInteger,
                             @UserInput(name="aBoolean") Boolean aBoolean){
            throw new UnsupportedOperationException();
        }
    }

    @Job
    public static class JobWithDefaultValueArgs {
        @JobMethod
        public String theJob(@UserInput(name="theArg", defaultValue = "a-default") String theArg){
            throw new UnsupportedOperationException();
        }
    }

    @Job
    public static class JobWithIntegerDefaultValueArgs {
        @JobMethod
        public String theJob(@UserInput(name="theArg",  defaultValue = "123") Integer theArg){
            throw new UnsupportedOperationException();
        }
    }


    ///////////////////////////////
    // Illegal Jobs

    public static class NotAJob {}

    @Job
    public static class WithoutJobMethod {
        private String notAPublicMethod(){
            throw new UnsupportedOperationException();
        }
    }

    @Job
    public static class TwoJobMethods {
        @JobMethod
        public String firstMethod(){
            throw new UnsupportedOperationException();
        }
        @JobMethod
        public String secondMethod(){
            throw new UnsupportedOperationException();
        }
    }


    ///////////////////////////////
    // Full Example
    @Job(group="test-jobs", value = "complex-job", description = "the description")
    public static class JobWithAllAnnotationsSet {
        @JobMethod
        public String theJobMethod(@UserInput(name="aString", defaultValue = "abc", description = "desc-aString") String aString,
                                   @UserInput(name="anInteger", defaultValue = "123", description = "desc-anInteger") Integer anInteger,
                                   @UserInput(name="aBoolean", defaultValue = "true", description = "desc-aBoolean") Boolean aBoolean){
            throw new UnsupportedOperationException();
        }
    }




}
