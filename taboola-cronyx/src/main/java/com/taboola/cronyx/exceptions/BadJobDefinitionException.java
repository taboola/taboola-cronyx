package com.taboola.cronyx.exceptions;

public class BadJobDefinitionException extends CronyxException {

    public BadJobDefinitionException() {
    }

    public BadJobDefinitionException(String message) {
        super(message);
    }

    public BadJobDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadJobDefinitionException(Throwable cause) {
        super(cause);
    }

    public BadJobDefinitionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
