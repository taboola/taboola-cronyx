package com.taboola.cronyx;

public interface CommandExecutor {

    /**
     * Execute a remote shell command
     * @param command A string representing a shell command to be executed against Cronyx
     * @return A {@code CommandResponse} object. This object will either be of type {@code CommandResponse.SuccessfulResponse} in case the command
     * executed successfully and will contain any return value supplied by the command or it will return a result of type {@code CommandResponse.FailedResponse}
     * if the command failed, in which case the returned object will contain whatever error thrown and a corresponding error message
     */
    CommandResponse execute(String command);
}
