package com.taboola.cronyx.autoconfigure;

import com.taboola.cronyx.CommandExecutor;
import com.taboola.cronyx.crash.CrashCommandExecutor;
import org.crsh.plugin.PluginLifeCycle;
import org.crsh.shell.ShellFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.CrshAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

import java.io.File;

@Configuration
@Import({CronyxAutoConfiguration.class, CrshAutoConfiguration.class})
public class CrashCommandLineAutoConfiguration {

    @Bean(name = "commandExecutor")
    public CommandExecutor commandExecutor(@Qualifier("shellBootstrap") PluginLifeCycle crshBootstrapBean) {
        ShellFactory shellFactory = crshBootstrapBean.getContext().getPlugin(ShellFactory.class);
        return new CrashCommandExecutor(shellFactory.create(null));
    }

    @Configuration
    @ConditionalOnProperty(prefix = "cronyx", name = "interactive")
    @DependsOn("mainScheduler")
    @AutoConfigureAfter(CrshAutoConfiguration.class)
    public static class ShellConfiguration {

        @Bean
        InteractiveShellRunner runner(@Qualifier("shellBootstrap") PluginLifeCycle crshBootstrapBean){
            return new InteractiveShellRunner(crshBootstrapBean);
        }

    }

    @Configuration
    @ConditionalOnProperty(prefix = "cronyx", name = "script")
    @DependsOn("mainScheduler")
    @AutoConfigureAfter(CrshAutoConfiguration.class)
    public static class ScriptConfiguration {

        @Bean
        ScriptRunner runner(@Qualifier("commandExecutor") CommandExecutor commandExecutor,
                            @Value("${cronyx.script}") File script){
            return new ScriptRunner(commandExecutor, script);
        }

    }

}
