package commands

import com.taboola.cronyx.*
import com.taboola.cronyx.crash.TriggerCommandFormatter
import com.taboola.cronyx.util.crash.CompleterUtils
import org.crsh.cli.*
import org.crsh.cli.completers.EnumCompleter
import org.crsh.cli.completers.FileCompleter
import org.crsh.cli.descriptor.ParameterDescriptor
import org.crsh.cli.spi.Completer
import org.crsh.cli.spi.Completion
import org.crsh.command.InvocationContext
import org.crsh.groovy.GroovyCommand
import org.springframework.beans.factory.BeanFactory

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

import static com.taboola.cronyx.TriggerDefinitionBuilder.*
import static org.quartz.CronExpression.isValidExpression

class trigger extends GroovyCommand implements Completer {
    //string in the form of name=value
    static JOB_ARGUMENT_PATTERN = ~/[^=]+=[^=]+/
    //dot delimited list of strings, each composed of alphanumeric characters and underscores only
    static NAME_AND_GROUP_PATTERN = ~/([\w\d]+\.)*[\w\d]+/
    static dateFormatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HHmmss_SSS")
    static commandFormatter = new TriggerCommandFormatter()

    @Usage("list all available trigger groups")
    @Man("""Lists all of the groups of registered triggers in this instance of cronyx.
            """)
    @Command
    def groups(InvocationContext context) {
        for(def jd : schedulingService().getTriggerGroupNames()) {
            out << jd << reset << "\r\n"
        }
    }

    @Usage("list available triggers")
    @Man("""Lists all of the registered triggers in this instance of cronyx.
            As an option it is possible to filter triggers based on groups, and or based on currently running filter.
            """)
    @Command
    def list(InvocationContext context,
             @Usage("list all triggers in the specified group") @Option(names = ["group", "g"],  completer = trigger.class) String group,
             @Usage("only return triggers that are currently running") @Option(names = ["running", "r"], completer = trigger.class) Boolean running,
             @Usage("only return triggers running on the local scheduler, must be used with -r") @Option(names = ["local", "l"],  completer = trigger.class) Boolean local) {
        def foundTriggers = (running ? executingTriggersService().getCurrentlyExecutingTriggers(local ?: false) : schedulingService().getAllTriggers())

        if (group != null) {
            foundTriggers = foundTriggers.stream().filter({ trg -> trg.getTriggerKey().getGroup() == group }).collect(Collectors.toList())
        }

        if (foundTriggers.size() <= 0) {
            return "could not find any triggers that fit the criteria"
        }

        prettyTriggerList(context, foundTriggers)
    }

    @Usage("print info about a trigger")
    @Man("""prints information about a specific trigger, that was identified  by key.
            """)
    @Command
    def info(InvocationContext context,
             @Required @Argument(name = "triggerKey", completer = trigger.class) String triggerKey) {
        def pair = triggerKey.split('\\.(?=[^.]*$)') //splits by last dot
        def trigger = schedulingService().getTriggerByKey(new NameAndGroup(pair[1], pair[0]))

        context.provide(trigger)
    }


    @Usage("execute a job as soon as possible")
    @Command
    def now(InvocationContext context,
            @Usage("qualified job name") @Required @Argument(completer = trigger.class)
                    String jobKey,
            @Usage("optional job-specific arguments in key=value form") @Argument
                    List<String> jobArgs,
            @Usage("misfire handling behavior. valid values: DROP, FIRE_NOW")
            @Option(names = ["misfire", "m"], completer = EnumCompleter.class)
                    Immediate.MisfireInstruction misfire,
            @Usage("Provided key for the trigger")
            @Man("trigger key comprised of name and group. if the specified name does not contain a dot, a group will not be set")
            @Option(names = ["name", "n"], completer = NewTriggerCompleter.class)
                    String qualifiedTriggerName
    ) {

        if (qualifiedTriggerName && !validName(qualifiedTriggerName)) {
            throw new IllegalArgumentException("trigger name must be a list of dot delimited strings of length greater than zero")
        }

        if (!validName(jobKey)) {
            throw new IllegalArgumentException("job name must be a list of dot delimited strings of length greater than zero")
        }

        if (!validJobArguments(jobArgs)) {
            throw new IllegalArgumentException("all job arguments must be of the form key=value")
        }

        def immediateTrigger = immediate().identifiedAs(toNameAndGroup(qualifiedTriggerName ?: arbitraryTriggerName()))
                                 .forJob(toNameAndGroup(jobKey))
                                 .withData(toMap(jobArgs))
                                 .withMisfireInstruction(misfire ?: Immediate.MisfireInstruction.DROP)
                                 .build()
        schedulingService().saveOrUpdateTrigger(immediateTrigger)
        context.provide(immediateTrigger)
    }

    @Usage("schedule a cron job")
    @Command
    def cron(InvocationContext context,
             @Usage("Provided key for the trigger")
             @Man("trigger key comprised of name and group. if the specified name does not contain a dot, a group will not be set")
             @Required @Argument(completer = NewTriggerCompleter.class)
                     String qualifiedTriggerName,
             @Usage("qualified job name") @Required @Argument(completer = trigger.class)
                     String jobKey,
             @Usage ("valid cron scheduling expression") @Required
             @Argument(completer = CronExampleCompleter.class)
                     String cronExpression,
             @Usage("optional job-specific arguments in key=value form") @Argument
                     List<String> jobArgs,
             @Usage("misfire handling behavior. valid values are: DROP, FIRE_ONCE")
             @Option(names = ["misfire", "m"], completer = EnumCompleter.class)
                     Cron.MisfireInstruction misfire
    ) {

        if (!validName(qualifiedTriggerName)) {
            throw new IllegalArgumentException("trigger name must be a list of dot delimited strings of length greater than zero")
        }

        if (!validName(jobKey)) {
            throw new IllegalArgumentException("job name must be a list of dot delimited strings of length greater than zero")
        }

        if (!validJobArguments(jobArgs)) {
            throw new IllegalArgumentException("all job arguments must be of the form key=value")
        }

        if (!isValidExpression(cronExpression)) {
            throw new IllegalArgumentException("specified expression is not a valid cron expression")
        }

        def cronTrigger = cron().identifiedAs(toNameAndGroup(qualifiedTriggerName))
                .forJob(toNameAndGroup(jobKey))
                .withCronExpression(cronExpression)
                .withData(toMap(jobArgs))
                .withMisfireInstruction(misfire)
                .build()
        schedulingService().saveOrUpdateTrigger(cronTrigger)
        schedulingService().getTriggerByKey(cronTrigger.getTriggerKey())
    }

    @Usage("delete an existing trigger")
    @Command
    def delete(InvocationContext context,
               @Usage("a list of qualified trigger names you wish to delete")
               @Required @Argument(completer = trigger.class) List<String> triggerKey) {

        if (!triggerKey.every { validName(it) }) {
            throw new IllegalArgumentException("trigger name must be a list of dot delimited strings of length greater than zero")
        }

        triggerKey.each {
            def asNameAndGroup = toNameAndGroup(it)
            schedulingService().removeTrigger(asNameAndGroup)
        }

        return "triggers deleted successfully"
    }

    @Usage("pauses selected triggers, preventing any future execution. this command does not kill any currently executing trigger")
    @Command
    def pause(InvocationContext context,
              @Man("pause a job or a job group. supports regular expressions")
              @Option(names = ["job", "j"], completer = trigger) String jobKey,
              @Man("pauses the specified triggers. supports regular expressions")
              @Argument(completer = trigger) String triggerKey) {

        def confirmation
        def triggersToPause =
                jobKey ?
                schedulingService().getAllTriggers().findAll { it.getJobKey().toString() ==~ jobKey } :
                schedulingService().getAllTriggers().findAll { it.getTriggerKey().toString() ==~ triggerKey }
        if (triggersToPause.size() > 1) {
            confirmation = context.readLine("You are about to pause the following triggers:\n${triggersToPause.collect { it.getTriggerKey() }.join("\n")}\nAre you certain you wish to proceed [Y/N]?", false)
        }
        if (triggersToPause.size() <= 1 || confirmation.toLowerCase() == "y" || confirmation.toLowerCase() == "yes") {
            triggersToPause.each { schedulingService().pauseTrigger(it.getTriggerKey()) }
            "triggers paused successfully, skipped non-cron triggers"
        } else {
            "no action performed"
        }
    }

    @Usage("resumes selected triggers, applying their misfire policy whenever a firing was missed during the pause period")
    @Command
    def resume(InvocationContext context,
               @Man("resumes a job or job group")
               @Option(names = ["job", "j"], completer = trigger) String jobKey,
               @Man("resumes the specified triggers. supports regular expressions")
               @Argument(completer = trigger) String triggerKey) {

        def confirmation
        def triggersToResume =
                jobKey ?
                schedulingService().getAllTriggers().findAll { it.getJobKey().toString() ==~ jobKey } :
                schedulingService().getAllTriggers().findAll { it.getTriggerKey().toString() ==~ triggerKey }
        if (triggersToResume.size() > 1) {
            confirmation = context.readLine("You are about to resume the following triggers:\n${triggersToResume.collect { it.getTriggerKey() }.join("\n")}\nAre you certain you wish to proceed [Y/N]?", false)
        }
        if (triggersToResume.size() <= 1 || confirmation.toLowerCase() == "y" || confirmation.toLowerCase() == "yes") {
            triggersToResume.each { schedulingService().resumeTrigger(it.getTriggerKey()) }
            "triggers resumed successfully"
        } else {
            "no action performed"
        }
    }

    @Usage("export selected triggers to command format, creating a valid trigger command that can be executed against the shell")
    @Command
    def export(InvocationContext context,
               @Man("export triggers to a text file at the specified location")
               @Option(names = ["output", "o"], completer = FileCompleter) String pathToFile,
               @Man("triggerKey to export. supports regular expressions")
               @Argument(completer = trigger) String triggerKey) {

        def triggersToExport =
                triggerKey ?
                schedulingService().getAllTriggers().findAll { it.getTriggerKey().toString() ==~ triggerKey } :
                schedulingService().getAllTriggers()
        String allTriggersFormatted =
                triggersToExport.collect { commandFormatter.format(it) }
                                .findAll { it != null }
                                .join("\n")
        if (pathToFile) {
            def file = new File(pathToFile)
            file << allTriggersFormatted
        } else {
            return allTriggersFormatted
        }
    }

    @Usage("schedule a job to run after a specific trigger")
    @Command
    def after(InvocationContext context,
              @Usage("Provided key for the trigger")
              @Man("trigger key comprised of name and group. if the specified name does not contain a dot, a group will not be set")
              @Required @Argument(completer = NewTriggerCompleter.class)
                      String qualifiedTriggerName,
              @Usage("qualified job name") @Required @Argument(completer = trigger.class)
                      String jobKey,
              @Usage("previous trigger names, comma separated") @Required @Argument(completer = trigger.class)
                      String previousTriggers,
              @Usage("job-specific arguments in key=value form") @Argument
                      List<String> jobArgs
    ) {

        if (qualifiedTriggerName && !validName(qualifiedTriggerName)) {
            throw new IllegalArgumentException("trigger name must be a list of dot delimited strings of length greater than zero")
        }

        if (!validName(jobKey)) {
            throw new IllegalArgumentException("job name must be a list of dot delimited strings of length greater than zero")
        }

        if(!previousTriggers) {
            throw new IllegalArgumentException("previous triggers must be a list of comma delimited strings of length greater than zero")
        }

        List<String> previousTriggersList = Arrays.asList(previousTriggers.split(","))*.trim()
        for(String previousTrigger : previousTriggersList) {
            if (!validName(previousTrigger)) {
                throw new IllegalArgumentException("trigger name must be a list of dot delimited strings of length greater than zero. Offending name: " + previousTrigger)
            }
        }

        if (!validJobArguments(jobArgs)) {
            throw new IllegalArgumentException("all job arguments must be of the form key=value")
        }

        def trigger = after()
                .identifiedAs(toNameAndGroup(qualifiedTriggerName))
                .forJob(toNameAndGroup(jobKey))
                .withData(toMap(jobArgs))
                .withPreviousTriggers(toNameAndGroupList(previousTriggersList))
                .build()

        schedulingService().saveOrUpdateTrigger(trigger)
    }

    @Override
    Completion complete(ParameterDescriptor parameter, String prefix) throws Exception {
        def strings
        if (parameter.hasProperty("names") && (parameter.names.contains('running') || parameter.names.contains('local'))) {
            strings = ["true", "false"]
        } else if (parameter.hasProperty("names") && parameter.names.contains('group')) {
            strings = schedulingService().getTriggerGroupNames()
                                         .findAll { it.startsWith(prefix) }
        } else if (parameter.hasProperty("name") && parameter.name == 'triggerKey') {
            strings = schedulingService().getAllTriggers()
                                         .collect { it.getTriggerKey().getGroup() + "." + it.getTriggerKey().getName() }
                                         .findAll { it.startsWith(prefix) }
        } else if (parameter.hasProperty("name") && parameter.name == 'previousTriggers') {
            def leadingNames = ""
            if (prefix && prefix.contains(",")) {
                prefix = prefix.replace(' ', '')
                leadingNames = prefix.substring(0, prefix.lastIndexOf(",") + 1)
            }

            strings = schedulingService().getAllTriggers()
                                         .collect { leadingNames + it.getTriggerKey().getGroup() + "." + it.getTriggerKey().getName() }
                                         .findAll { it.startsWith(prefix) }
        } else if ((parameter.hasProperty("name") && parameter.name == "jobKey") ||
                (parameter.hasProperty("names") && parameter.names.contains("job"))) {

            strings = jobService().getAllJobs()
                                  .collect { it.getKey().getGroup() + "." + it.getKey().getName() }
                                  .findAll { it.startsWith(prefix) }
        } else if (parameter.hasProperty("names") && parameter.names.contains("jobGroup")) {
            strings = jobService().getJobGroupNames()
                                  .findAll { it.startsWith(prefix) }
        }

        return CompleterUtils.getCompletion(strings, prefix)
    }

    def schedulingService() {
        BeanFactory beanFactory = context.getAttributes()['spring.beanfactory'] as BeanFactory
        beanFactory.getBean("reloadableSchedulingService", SchedulingService)
    }

    def jobService() {
        BeanFactory beanFactory = context.getAttributes()['spring.beanfactory'] as BeanFactory
        beanFactory.getBean(JobsService)
    }

    def executingTriggersService() {
        BeanFactory beanFactory = context.getAttributes()['spring.beanfactory'] as BeanFactory
        beanFactory.getBean(ExecutingTriggersService)
    }

    def getTriggersMatchingExpression(String expression) {
        if (expression == '*') {
            schedulingService().getAllTriggers()
        } else if (expression.endsWith('.*')) {
            def triggerGroup = expression[0..-3]
            schedulingService().getTriggersOfGroup(triggerGroup)
        } else {
            [schedulingService().getTriggerByKey(toNameAndGroup(expression))]
        }
    }

    static toMap(List<String> jobArgs) {
        if (!jobArgs) {
            return Collections.emptyMap()
        } else {
            return jobArgs.collectEntries {
                def (name, value) = it.tokenize('=')
                return [name, value]
            }
        }
    }

    static toNameAndGroup(String qualifiedName) {
        def split = qualifiedName.tokenize('.')
        if (split.size() == 1) {
            return new NameAndGroup(split.last(), null)
        } else {
            return new NameAndGroup(split.last(), split[0..split.size() - 2].join("."))
        }
    }

    static toNameAndGroupList(List<String> qualifiedNames) {
        List<NameAndGroup> nameAndGroups = new ArrayList<>(qualifiedNames.size())
        for(String name : qualifiedNames) {
            nameAndGroups.add(toNameAndGroup(name))
        }
        return nameAndGroups
    }

    static validJobArguments(List<String> args) {
        !args || args.stream().allMatch {it ==~ JOB_ARGUMENT_PATTERN}
    }

    static validName(String name) {
        name && name ==~ NAME_AND_GROUP_PATTERN
    }

    static class NewTriggerCompleter implements Completer {
        @Override
        Completion complete(ParameterDescriptor parameter, String prefix) throws Exception {
            if (prefix == null || prefix == "") {
                return Completion.create(arbitraryTriggerName(), true)
            } else {
                return Completion.create()
            }
        }
    }


    static class CronExampleCompleter implements Completer {

        @Override
        Completion complete(ParameterDescriptor parameter, String prefix) throws Exception {
            if (prefix == null || prefix == "") {
                return Completion.create("0 0 0 * * ?", true)
            } else {
                return Completion.create()
            }
        }
    }

    static prettyTriggerList(InvocationContext context, List<TriggerDefinition> triggers) {
        triggers.toSorted { t1, t2 ->
            t1.getJobKey().getGroup() <=> t2.getJobKey().getGroup() ?:
            t1.getJobKey().getName() <=> t2.getJobKey().getName() ?:
            t1.getTriggerKey().getGroup() <=> t2.getTriggerKey().getGroup() ?:
            t1.getTriggerKey().getName() <=> t2.getTriggerKey().getName()
        }.forEach {
            td -> context.provide(td)
        }
        return triggers.size()
    }

    static arbitraryTriggerName() {
        LocalDateTime date = LocalDateTime.now()
        return "DEFAULT.Trigger_" + date.format(dateFormatter)
    }
}