package com.taboola.cronyx.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.codahale.metrics.MetricRegistry;
import com.taboola.cronyx.EnableCronyx;
import com.taboola.cronyx.autoconfigure.CrashCommandLineAutoConfiguration;
import com.taboola.cronyx.testing.utils.SQLScriptUtil;
import com.taboola.scheduler.jobs.CronyxJobBundle;

@Configuration
@Import({CrashCommandLineAutoConfiguration.class,
        CronyxJobBundle.class})
@EnableCronyx
public class CronyxServiceConfig {

    @Bean(name = "metricRegistry")
    public MetricRegistry metricRegistry() {
        return new MetricRegistry();
    }

    @Bean(name="cronyxDataSource")
    public DataSource cronyxDatasource() throws SQLException, URISyntaxException, IOException {

        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:cronyx;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MVCC=TRUE;INIT=CREATE SCHEMA IF NOT EXISTS cronyx\\;SET SCHEMA cronyx\\;");

        SQLScriptUtil.runScript(ds, "sql/tables_h2.sql");

        return ds;

    }

}
