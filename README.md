# taboola-cronyx - Job scheduling framework 
this framwork is used in taboola, its a Job scheduling framework that is based on quartz and spring. 
its main advantages are: 
 - easy and quick start. 
 - rest api support. 
 - manage your triggers through easy to use cli shell. 
 - extended quartz capabilities / features. e.g - chain triggers (fan-in support), history of each execution, auto retry, and more.
 
 **Requires JDK 1.8 or higher
 
 how do you run it? 
 to make it easy - we included an example of a service based on spring-boot(under taboola-cronyx-server-exmaple). to run it execute the    CronyxService main class. then ssh into the shell by running ssh user@localhost -p 2000, password pass.
 in the shell you have different commands to manage your jobs / triggers 
 just write "job -h" or "trigger -h" and see all the different options. 
 e.g - execute a trigger by running - trigger now -n <triggerGroup.triggerName> <jobGroup.jobName> <extra parameters>
 
 how do i add a job? 
 just follow the helloWorld example - add a class under the job bundle package with the @job annotation , give it a group and a name  - e.g @Job(group = "example", name = "HelloWorld") , and add a method with @JobMethod annotation. 
 
 how do i add a cron trigger? 
 ssh into the shell and run - trigger cron <triggerGroup.triggerName> <jobGroup.jobName> "<cron expression>", e.g trigger cron example.runEveryMinute example.HelloWorld "0 0/1 * 1/1 * ? *" 
 
 how do i run commands through the rest api? 
 you can open your browser and write - localhost:8080/triggers/all - this will give you a list of all the triggers. 
 if you want to create a trigger you can do it with curl: 
 curl -f -s -H 'Content-Type: application/json' -X POST localhost:8080/triggers/new --data '{"triggerKey":{"group":"example","name":"HelloWorld22"}, "jobKey":{"group":"example","name":"HelloWorld"},"misfireInstruction":"FIRE_ONCE","cronExpression":"0 0/1 * 1/1 * ? *"}'
 
 Be sure to go over taboola-cronyx-api and see all the annontations that we have.
 
 Your Improvements

If you add improvements to taboola-cronyx please send them to us as pull requests on github. We will add them to the next release so that everyone can enjoy them. You might also benefit from it: others may fix bugs in your source files or may continue to enhance them.
