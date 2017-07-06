package com.taboola.cronyx.autoconfigure;

import com.taboola.cronyx.CommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.nio.file.Files;

public class ScriptRunner implements ApplicationContextAware, CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ScriptRunner.class);

    final private CommandExecutor commandExecutor;
    final private File script;

    private ConfigurableApplicationContext applicationContext;

    public ScriptRunner(CommandExecutor commandExecutor, File script) {
        this.commandExecutor = commandExecutor;
        this.script = script;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }


    @Override
    public void run(String... args) throws Exception {

        Files.lines(script.toPath())
                .filter(s -> {
                    logger.info(s);
                    return true;
                })
                .map(commandExecutor::execute)
                .map(String::valueOf)
                .forEach(logger::info);

        applicationContext.close();
    }
}
