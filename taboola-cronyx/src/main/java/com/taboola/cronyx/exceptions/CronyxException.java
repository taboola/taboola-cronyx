package com.taboola.cronyx.exceptions;

public class CronyxException extends RuntimeException {

    public CronyxException() {
    }

    public CronyxException(String message) {
        super(message);
    }

    public CronyxException(String message, Throwable cause) {
        super(message, cause);
    }

    public CronyxException(Throwable cause) {
        super(cause);
    }

    public CronyxException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
