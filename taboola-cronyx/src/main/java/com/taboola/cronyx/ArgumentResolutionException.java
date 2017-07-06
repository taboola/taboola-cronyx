package com.taboola.cronyx;

public class ArgumentResolutionException extends CronyxTerminalException {

    public ArgumentResolutionException() {
    }

    public ArgumentResolutionException(String message) {
        super(message);
    }

    public ArgumentResolutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArgumentResolutionException(Throwable cause) {
        super(cause);
    }

    public ArgumentResolutionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
