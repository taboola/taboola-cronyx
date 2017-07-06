package com.taboola.cronyx.autoconfigure;

import static com.taboola.cronyx.impl.ListenerMatcher.jobCompletedSuccessfully;
import static com.taboola.cronyx.util.EnvironmentUtil.changePrefix;
import static com.taboola.cronyx.util.EnvironmentUtil.findProps;
import static com.taboola.cronyx.util.MetricUtil.createLastExecutionMetricName;
import static org.quartz.impl.StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import com.taboola.cronyx.impl.ReloadableSchedulingService;
import org.apache.commons.lang3.tuple.Pair;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerListener;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.mvc.MvcEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.codahale.metrics.MetricRegistry;
import com.taboola.cronyx.ArgumentResolver;
import com.taboola.cronyx.CronyxExecutionContext;
import com.taboola.cronyx.ExecutingTriggersService;
import com.taboola.cronyx.FiringListener;
import com.taboola.cronyx.JobsService;
import com.taboola.cronyx.ListenerRegistryKey;
import com.taboola.cronyx.Registry;
import com.taboola.cronyx.SchedulingService;
import com.taboola.cronyx.builtin.jobs.BuiltInJobBundle;
import com.taboola.cronyx.impl.AfterDAO;
import com.taboola.cronyx.impl.AfterProcessor;
import com.taboola.cronyx.impl.ConcurrentRegistry;
import com.taboola.cronyx.impl.DefaultJobsService;
import com.taboola.cronyx.impl.DelegatingListener;
import com.taboola.cronyx.impl.DelegatingQuartzJob;
import com.taboola.cronyx.impl.DelegatingQuartzJobFactory;
import com.taboola.cronyx.impl.HistorianDAO;
import com.taboola.cronyx.impl.HistorianEntryMapper;
import com.taboola.cronyx.impl.JavaJobIntrospecter;
import com.taboola.cronyx.impl.JobIntrospecter;
import com.taboola.cronyx.impl.ListenerMatcher;
import com.taboola.cronyx.impl.MarkerGauge;
import com.taboola.cronyx.impl.NameAndGroupGraphValidator;
import com.taboola.cronyx.impl.NameAndGroupOrderedPairMapper;
import com.taboola.cronyx.impl.NameAndGroupRowMapper;
import com.taboola.cronyx.impl.NonConcurrentJob;
import com.taboola.cronyx.impl.Processor;
import com.taboola.cronyx.impl.QuartzDirectDBAccessService;
import com.taboola.cronyx.impl.QuartzJobRegistrarBeanFactoryPostProcessor;
import com.taboola.cronyx.impl.QuartzSchedulerServiceImpl;
import com.taboola.cronyx.impl.QuartzTriggerListenerAdapter;
import com.taboola.cronyx.impl.SimpleArgumentResolver;
import com.taboola.cronyx.impl.StdAfterDAO;
import com.taboola.cronyx.impl.StdHistorianDAO;
import com.taboola.cronyx.impl.StdNameAndGroupGraphValidator;
import com.taboola.cronyx.impl.converter.JobExecutionContextToFiringContext;
import com.taboola.cronyx.impl.converter.cronyxtoquartz.CronyxToQuartzConverter;
import com.taboola.cronyx.impl.converter.cronyxtoquartz.CronyxToQuartzSelector;
import com.taboola.cronyx.impl.converter.quartztocronyx.QuartzToCronyxConverter;
import com.taboola.cronyx.impl.converter.quartztocronyx.QuartzToCronyxSelector;
import com.taboola.cronyx.rest.JobServiceRestController;
import com.taboola.cronyx.rest.SchedulingServiceRestController;
import com.taboola.cronyx.types.LocalDatePropertyEditor;
import com.taboola.cronyx.types.LocalDateTimePropertyEditor;
import com.taboola.cronyx.util.QuartzJobClassSelector;

@Configuration
@Import(BuiltInJobBundle.class)
@ConditionalOnProperty(prefix = "cronyx", name = "enabled", havingValue = "true")
@EnableTransactionManagement
@EnableScheduling
public class CronyxAutoConfiguration {

    @Bean
    public JobsService defaultJobsService(@Qualifier("mainScheduler") Scheduler scheduler) {
        return new DefaultJobsService(scheduler);
    }

    @Bean(name = "schedulingService")
    public SchedulingService defaultSchedulingService(@Qualifier("mainScheduler") Scheduler scheduler,
                                                      @Qualifier("defaultJobsService") JobsService defaultJobsService,
                                                      @Qualifier("cronyxToQuartzSelector") CronyxToQuartzSelector cronyxToQuartzSelector,
                                                      @Qualifier("quartzToCronyxSelector") QuartzToCronyxSelector quartzToCronyxSelector,
                                                      @Qualifier("afterDAO") AfterDAO afterDAO,
                                                      @Qualifier("graphValidator") NameAndGroupGraphValidator graphValidator) {
        return new QuartzSchedulerServiceImpl(scheduler, defaultJobsService, cronyxToQuartzSelector, quartzToCronyxSelector, afterDAO, graphValidator);
    }

    @Bean(name = "reloadableSchedulingService")
    public ReloadableSchedulingService reloadableSchedulingService(@Qualifier("schedulingService") SchedulingService schedulingService) {
        return new ReloadableSchedulingService(schedulingService, new ConcurrentHashMap<>());
    }

    @Bean(name = "mainScheduler")
    public SchedulerFactoryBean mainScheduler(DelegatingQuartzJobFactory jobFactory,
                                                @Qualifier("quartzProperties") Properties quartzProperties,
                                                @Value("#{'${cronyx.passive}'=='true'}") boolean passive,
                                                @Qualifier("cronyxDataSource") DataSource dataSource){
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setJobFactory(jobFactory);
        factory.setQuartzProperties(quartzProperties);
        factory.setDataSource(dataSource);
        factory.setAutoStartup(!passive);
        factory.setSchedulerName(quartzProperties.getProperty(PROP_SCHED_INSTANCE_NAME));
        return factory;
    }

    @Bean(name = "schedulerName")
    public String schedulerName(@Qualifier("mainScheduler") Scheduler scheduler) throws SchedulerException {
        return scheduler.getSchedulerName();
    }

    @Bean(name = "executingTriggersService")
    public ExecutingTriggersService executingTriggersService(@Qualifier("cronyxDataSource") DataSource dataSource,
                                                             @Qualifier("schedulingService") SchedulingService schedulingService,
                                                             @Qualifier("schedulerName") String schedulerName) {
        return new QuartzDirectDBAccessService(dataSource, schedulingService, schedulerName);
    }

    @Bean(name = "listenerRegistry")
    public Registry<String, Pair<ListenerMatcher, FiringListener>> listenerRegistry(List<ListenerRegistryKey> listenerKeys) throws SchedulerException {
        Registry<String, Pair<ListenerMatcher, FiringListener>> listenerRegistry = new ConcurrentRegistry<>();
        for (ListenerRegistryKey listenerKey : listenerKeys) {
            listenerRegistry.register(
                    listenerKey.name,
                    Pair.of(listenerKey.matcher, listenerKey.listener));
        }
        return listenerRegistry;
    }

    @Bean(name = "listenerAdapter")
    public TriggerListener listenerAdapter(@Qualifier("listenerRegistry") Registry<String, Pair<ListenerMatcher, FiringListener>> listenerRegistry,
                                           @Qualifier("contextRegistry") Registry<String, CronyxExecutionContext> contextRegistry,
                                           @Qualifier("mainScheduler") Scheduler scheduler,
                                           @Qualifier("jobExecutionContextToFiringContext") JobExecutionContextToFiringContext jobExecutionContextToFiringContext) throws SchedulerException {
        TriggerListener adapter = new QuartzTriggerListenerAdapter(listenerRegistry, contextRegistry, jobExecutionContextToFiringContext);
        scheduler.getListenerManager().addTriggerListener(adapter);
        return adapter;
    }

    @Bean
    public ListenerRegistryKey afterJobCompleteFiringListener(MetricRegistry metricRegistry) {
        FiringListener listener = ctx -> {
            String gaugeName = createLastExecutionMetricName(ctx.getFiredTrigger());
            MarkerGauge gauge = (MarkerGauge) metricRegistry.getGauges().get(gaugeName);
            if (gauge == null) {
                gauge = metricRegistry.register(gaugeName, new MarkerGauge());
            }
            gauge.mark();
        };

        return new ListenerRegistryKey(
                "successfulExecutionMarker",
                jobCompletedSuccessfully(),
                listener
        );
    }

    @Bean(name = "contextRegistry")
    public Registry<String, CronyxExecutionContext> contextRegistry() {
        return new ConcurrentRegistry<>();
    }

    @Bean(name = "quartzProperties")
    public Properties quartzProperties(ConfigurableEnvironment configurableEnvironment){
        Properties found = findProps(configurableEnvironment, "cronyx.org.quartz");
        return changePrefix(found, "cronyx.org.quartz", "org.quartz");
    }

    @Bean
    public DelegatingQuartzJobFactory quartzJobFactory(){
        return new DelegatingQuartzJobFactory();
    }

    @Bean(name = "classSelector")
    public QuartzJobClassSelector classSelector() {
        return jobDef -> jobDef.isConcurrentExecutionAllowed() ? DelegatingQuartzJob.class : NonConcurrentJob.class;
    }

    @Bean(name = "argumentResolver")
    public ArgumentResolver argumentResolver(BeanFactory beanFactory,
                                             TypeConverter typeConverter) {
        return new SimpleArgumentResolver(beanFactory, typeConverter);
    }

    @Bean
    public QuartzJobRegistrarBeanFactoryPostProcessor registerQuartzJobs(JobIntrospecter jobIntrospecter,
                                                                         Scheduler scheduler,
                                                                         QuartzJobClassSelector classSelector){
        return new QuartzJobRegistrarBeanFactoryPostProcessor(jobIntrospecter, scheduler, classSelector);
    }

    @Bean
    public MetricInitializerBeanPostProcessor initializeLastExecutionMetrics(MetricRegistry metricRegistry) {
        return new MetricInitializerBeanPostProcessor(metricRegistry);
    }

    @Bean
    public JobIntrospecter jobIntrospecter(){
        return new JavaJobIntrospecter();
    }

    @Bean
    public TypeConverter typeConverter() {
        SimpleTypeConverter tc = new SimpleTypeConverter();
        tc.registerCustomEditor(LocalDateTime.class, new LocalDateTimePropertyEditor("yyyyMMdd_HHmmss"));
        tc.registerCustomEditor(LocalDate.class, new LocalDatePropertyEditor("yyyyMMdd"));
        return tc;
    }

    @Bean(name = "triggerRestController")
    @ConditionalOnWebApplication
    public MvcEndpoint triggerRestController(@Qualifier("schedulingService") SchedulingService schedulingService) {
        return new SchedulingServiceRestController(schedulingService);
    }

    @Bean(name = "jobRestController")
    @ConditionalOnWebApplication
    public MvcEndpoint jobRestController(@Qualifier("defaultJobsService") JobsService jobSchedulingService) {
        return new JobServiceRestController(jobSchedulingService);
    }

    @Bean(name = "cronyxToQuartzSelector")
    public CronyxToQuartzSelector cronyxToQuartzSelector() {
        List<Pair<Class, CronyxToQuartzConverter>> classCronyxToQuartzConverterPairs = ConfigUtil.cronyxQuartzConverterPairs(CronyxToQuartzConverter.class);
        return new CronyxToQuartzSelector(classCronyxToQuartzConverterPairs);
    }

    @Bean(name = "quartzToCronyxSelector")
    public QuartzToCronyxSelector quartzToCronyxSelector() {
        List<Pair<Class, QuartzToCronyxConverter>> classQuartzToCronyxConverterPairs = ConfigUtil.cronyxQuartzConverterPairs(QuartzToCronyxConverter.class);
        return new QuartzToCronyxSelector(classQuartzToCronyxConverterPairs);
    }

    @Bean(name = "jobExecutionContextToFiringContext")
    public JobExecutionContextToFiringContext jobExecutionContextToFiringContext(@Qualifier("quartzToCronyxSelector") QuartzToCronyxSelector quartzToCronyxSelector) {
        return new JobExecutionContextToFiringContext(quartzToCronyxSelector);
    }

    @Bean(name = "afterDAO")
    public AfterDAO afterDAO(@Qualifier("mainScheduler") Scheduler scheduler,
                             @Qualifier("namedParameterJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                             @Qualifier("nameAndGroupRowMapper") NameAndGroupRowMapper nameAndGroupRowMapper,
                             @Qualifier("nameAndGroupOrderedPairMapper") NameAndGroupOrderedPairMapper nameAndGroupOrderedPairMapper) throws SchedulerException {
        return new StdAfterDAO(scheduler.getSchedulerName(), namedParameterJdbcTemplate, nameAndGroupRowMapper, nameAndGroupOrderedPairMapper);
    }

    @Bean(name = "afterProcessor")
    public Processor afterProcessor(@Qualifier("mainScheduler") Scheduler scheduler,
                                    @Qualifier("afterDAO") AfterDAO afterDAO,
                                    @Qualifier("historianDAO") HistorianDAO historianDAO){
        return new AfterProcessor(scheduler, afterDAO, historianDAO);
    }

    @Bean(name = "afterListener")
    public DelegatingListener afterListener(@Qualifier("afterProcessor") Processor afterProcessor) {
        return new DelegatingListener(afterProcessor);
    }

    @Bean(name = "historianDAO")
    public HistorianDAO historianDAO(@Qualifier("mainScheduler") Scheduler scheduler,
                                     @Qualifier("namedParameterJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                     @Qualifier("historianEntryMapper") HistorianEntryMapper historianEntryMapper) throws SchedulerException {
        return new StdHistorianDAO(scheduler.getSchedulerName(), namedParameterJdbcTemplate, historianEntryMapper);
    }

    @Bean(name = "namedParameterJdbcTemplate")
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(@Qualifier("cronyxDataSource") DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean(name = "nameAndGroupRowMapper")
    public NameAndGroupRowMapper nameAndGroupRowMapper() {
        return new NameAndGroupRowMapper();
    }

    @Bean(name = "nameAndGroupOrderedPairMapper")
    public NameAndGroupOrderedPairMapper nameAndGroupOrderedPairMapper() {
        return new NameAndGroupOrderedPairMapper();
    }

    @Bean(name = "historianEntryMapper")
    public HistorianEntryMapper historianEntryMapper() {
        return new HistorianEntryMapper();
    }

    @Bean
    public ListenerRegistryKey afterJobCompleteDependencyListener(@Qualifier("afterListener") DelegatingListener afterListener) {

        return new ListenerRegistryKey(
                "successfulAfterListener",
                jobCompletedSuccessfully(),
                afterListener
        );
    }

    @Bean(name = "platformTransactionManager")
    public PlatformTransactionManager platformTransactionManager(@Qualifier("cronyxDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "graphValidator")
    public NameAndGroupGraphValidator graphValidator(@Qualifier("mainScheduler") Scheduler scheduler) {
        return new StdNameAndGroupGraphValidator(scheduler);
    }

    @Bean(name = "schedulingServiceReloader")
    public Object schedulingServiceReloader(@Qualifier("reloadableSchedulingService") ReloadableSchedulingService schedulingService) {
        return new Object() {
            @Scheduled(fixedDelayString = "${cronyx.trigger-cache.reload-delay-millis:60000}")
            public void reloadCache() {
                schedulingService.reload();
            }
        };
    }
}