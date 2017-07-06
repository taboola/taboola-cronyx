package com.taboola.cronyx.crash;

import com.taboola.cronyx.CommandExecutor;
import com.taboola.cronyx.CommandResponse;
import com.taboola.cronyx.CommandResponse.FailedResponse;
import com.taboola.cronyx.CommandResponse.SuccessfulResponse;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.shell.ShellResponse.Error;
import org.crsh.shell.ShellResponse.UnknownCommand;
import org.crsh.text.Screenable;
import org.crsh.text.Style;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.crsh.shell.ShellResponse.*;

public class CrashCommandExecutor implements CommandExecutor {

    private Shell crashShell;

    public CrashCommandExecutor(Shell crashShell) {
        this.crashShell = crashShell;
    }

    @Override
    public CommandResponse execute(String command) {
        ShellProcess sp = crashShell.createProcess(command);
        CompletableFuture<ShellResponse> future = new CompletableFuture<>();
        sp.execute(new FutureAdapterContext(future));
        try {
            ShellResponse response = future.get();
            if (response instanceof Error) {
                return new FailedResponse(((Error) response).getThrowable(), response.getMessage());
            } else  if (response instanceof UnknownCommand) {
                return new FailedResponse(null, "the command specified does not exist");
            } else if (response instanceof Ok) {
                return new SuccessfulResponse(((Ok) response).getProduced());
            } else if (response instanceof Close) {
                return new SuccessfulResponse(null);
            } else {
                return new FailedResponse(null, "command failed");
            }
        } catch (Exception e) {
            return new FailedResponse(e, e.getMessage());
        }
    }

    private static class FutureAdapterContext implements ShellProcessContext {
        private final CompletableFuture<ShellResponse> completableFuture;

        public FutureAdapterContext(CompletableFuture<ShellResponse> completableFuture) {
            this.completableFuture = completableFuture;
        }

        @Override
        public void end(ShellResponse response) {
            completableFuture.complete(response);
        }

        @Override
        public boolean takeAlternateBuffer() throws IOException {
            return false;
        }

        @Override
        public boolean releaseAlternateBuffer() throws IOException {
            return false;
        }

        @Override
        public String getProperty(String propertyName) {
            return null;
        }

        @Override
        public String readLine(String msg, boolean echo) throws IOException, InterruptedException, IllegalStateException {
            System.out.println(msg);
            return msg;
        }

        @Override
        public int getWidth() {
            return 100;
        }

        @Override
        public int getHeight() {
            return 100;
        }

        @Override
        public void flush() throws IOException {

        }

        @Override
        public Screenable append(Style style) throws IOException {
            return null;
        }

        @Override
        public Screenable cls() throws IOException {
            return null;
        }

        @Override
        public Appendable append(CharSequence csq) throws IOException {
            System.out.print(csq);
            return this;
        }

        @Override
        public Appendable append(CharSequence csq, int start, int end) throws IOException {
            System.out.print(csq.subSequence(start, end));
            return this;
        }

        @Override
        public Appendable append(char c) throws IOException {
            System.out.print(c);
            return this;
        }
    }
}
