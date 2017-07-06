package com.taboola.cronyx;

import com.taboola.cronyx.exceptions.CronyxException;

public class CronyxTerminalException extends CronyxException {
    public CronyxTerminalException() {
    }

    public CronyxTerminalException(String message) {
        super(message);
    }

    public CronyxTerminalException(String message, Throwable cause) {
        super(message, cause);
    }

    public CronyxTerminalException(Throwable cause) {
        super(cause);
    }

    public CronyxTerminalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
