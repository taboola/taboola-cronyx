package com.taboola.cronyx.autoconfigure;

import org.crsh.console.jline.JLineProcessor;
import org.crsh.console.jline.Terminal;
import org.crsh.console.jline.TerminalFactory;
import org.crsh.console.jline.console.ConsoleReader;
import org.crsh.plugin.PluginLifeCycle;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellFactory;
import org.crsh.util.InterruptHandler;
import org.fusesource.jansi.AnsiConsole;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.*;

public class InteractiveShellRunner implements ApplicationContextAware, CommandLineRunner, InitializingBean, DisposableBean {

    final private PluginLifeCycle crshBootstrapBean;
    private Shell shell;
    private Terminal term;

    private ConfigurableApplicationContext applicationContext;


    public InteractiveShellRunner(PluginLifeCycle crshBootstrapBean) {
        this.crshBootstrapBean = crshBootstrapBean;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ShellFactory shellFactory = crshBootstrapBean.getContext().getPlugin(ShellFactory.class);
        shell = shellFactory.create(null);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    @Override
    public void destroy() throws Exception {
        try {
            if (term != null) {
                term.restore();
            }
        } catch (Exception ignore) {
        }
    }

    @Override
    public void run(String... args) throws Exception {

        if (shell != null) {

            term = TerminalFactory.create();

            //
            String encoding = jline.internal.Configuration.getEncoding();

            // Use AnsiConsole only if term doesn't support Ansi
            PrintStream out;
            PrintStream err;
            boolean ansi;
            if (term.isAnsiSupported()) {
                out = new PrintStream(new BufferedOutputStream(term.wrapOutIfNeeded(new FileOutputStream(FileDescriptor.out)), 16384), false, encoding);
                err = new PrintStream(new BufferedOutputStream(term.wrapOutIfNeeded(new FileOutputStream(FileDescriptor.err)), 16384), false, encoding);
                ansi = true;
            } else {
                out = AnsiConsole.out;
                err = AnsiConsole.err;
                ansi = false;
            }

            //
            FileInputStream in = new FileInputStream(FileDescriptor.in);
            ConsoleReader reader = new ConsoleReader(null, in, out, term);

            //
            final JLineProcessor processor = new JLineProcessor(ansi, shell, reader, out);

            //
            InterruptHandler interruptHandler = new InterruptHandler(processor::interrupt);
            interruptHandler.install();

            //
            Thread thread = new Thread(processor, "shell-line-processor");
            thread.setDaemon(true);
            thread.start();


            try {
                processor.closed();
                in.close();
            } catch (Throwable t) {
                t.printStackTrace();
            };

            applicationContext.close();
        }

    }

}
