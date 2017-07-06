package com.taboola.cronyx.impl;

import com.taboola.cronyx.exceptions.CronyxException;

public class SchedulingException extends CronyxException {
    public SchedulingException() {
    }

    public SchedulingException(String message) {
        super(message);
    }

    public SchedulingException(String message, Throwable cause) {
        super(message, cause);
    }

    public SchedulingException(Throwable cause) {
        super(cause);
    }

    public SchedulingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
