package commands

import com.taboola.cronyx.JobsService
import com.taboola.cronyx.NameAndGroup
import com.taboola.cronyx.util.crash.CompleterUtils
import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Man
import org.crsh.cli.Option
import org.crsh.cli.Required
import org.crsh.cli.Usage
import org.crsh.cli.descriptor.ParameterDescriptor
import org.crsh.cli.spi.Completer
import org.crsh.cli.spi.Completion
import org.crsh.command.InvocationContext
import org.crsh.groovy.GroovyCommand
import org.springframework.beans.factory.BeanFactory

class job extends GroovyCommand implements Completer {

    @Usage("lists registered jobs")
    @Man("""Uses this command to list the available jobs configured in this scheduler.
It is also has an optional 'group' option, which can filter the list to only include jobs from a specific group
            """)
    @Command
    def list(InvocationContext context, @Option(names = ["g", "group"], completer = job.class) String group) {
        def foundJobs;
        if (group?.trim()){
            foundJobs = getJobsService().getJobsOfGroup(group);
        } else {
            foundJobs = getJobsService().getAllJobs();
        }
        foundJobs = foundJobs.toSorted { j1, j2 ->
            j1.getKey().getGroup() <=> j2.getKey().getGroup() ?:
            j1.getKey().getName() <=> j2.getKey().getName()
        }
        for(def jd : foundJobs) {
            context.provide(jd);
        }
    }

    @Usage("lists job groups")
    @Man("""Lists all of the groups of registered jobs in this cronyx.
            """)
    @Command
    def groups(InvocationContext context) {
        for(def jd : getJobsService().getJobGroupNames()) {
            out << jd << reset << "\r\n";
        }
    }

    @Usage("shows the info of a specific job")
    @Man("""Lists all of the groups of registered jobs in this cronyx.
            """)
    @Command
    def info(InvocationContext context, @Required @Argument(name = "jobKey", completer = job.class) String jobKey) {
        def pair = jobKey.split('\\.');
        return jobsService.getJobByKey(new NameAndGroup(pair[1], pair[0]));
    }


    def getJobsService() {
        BeanFactory beanFactory = context.getAttributes()['spring.beanfactory'] as BeanFactory
        def jobsService = beanFactory.getBean("defaultJobsService", JobsService.class)
        return jobsService;
    }

    @Override
    Completion complete(ParameterDescriptor parameter, String prefix) throws Exception {

        def strings;
        if (parameter.hasProperty("name") && parameter.name == 'jobName') {
            strings = getJobsService().getAllJobs()
                    .stream()
                    .map({ jd -> jd.getKey().getName() })
                    .filter({ s -> s.startsWith(prefix) })
                    .collect()
        } else if (parameter.hasProperty("names") && parameter.names.contains('group')) {
            strings = getJobsService().getJobGroupNames()
                    .stream()
                    .filter({ s -> s.startsWith(prefix) })
                    .collect()
        } else if (parameter.hasProperty("name") && parameter.name == 'jobKey') {
            strings = getJobsService().getJobGroupNames()
                    .stream()
                    .flatMap({grp -> return getJobsService().getJobsOfGroup(grp).stream()})
                    .map({jd -> return jd.getKey().getGroup() + "." + jd.getKey().getName()})
                    .filter({ s -> s.startsWith(prefix) })
                    .collect()
        }

        return CompleterUtils.getCompletion(strings, prefix);

    }



}